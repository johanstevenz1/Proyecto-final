import com.vp.plugin.*;
import com.vp.plugin.diagram.*;
import com.vp.plugin.diagram.shape.IClassUIModel;
import com.vp.plugin.model.*;
import com.vp.plugin.model.factory.IModelElementFactory;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;

/**
 * Agrega al proyecto de Visual Paradigm las vistas 04 (desarrollo, arquitectura por capas) y 05
 * (despliegue) como modelos UML nativos y editables, construidos a partir del codigo real de
 * backend/ y frontend/. Conserva las vistas 01 a 03 del proyecto base y exporta las cinco a PNG.
 */
public class VistasDesarrolloDespliegue extends EntregaDiagramas {
  static final Color AZUL_CAPA = new Color(232, 240, 254), VERDE_CAPA = new Color(232, 245, 233);
  static final Color NARANJA_CAPA = new Color(253, 243, 228), MORADO_CAPA = new Color(243, 236, 250);
  static final Color GRIS_CAPA = new Color(245, 245, 245), BLANCO = Color.WHITE;
  static final int FUENTE = 14;

  final Map<String, IShapeUIModel> formas = new LinkedHashMap<>();

  @Override
  public void invoke(String[] argumentos) {
    try {
      Properties p = new Properties();
      try (InputStream in = new FileInputStream(new File(carpeta, "salida.properties"))) { p.load(in); }
      File salida = new File(p.getProperty("salida"));
      salida.mkdirs();
      app = ApplicationManager.instance();
      dm = app.getDiagramManager();
      f = IModelElementFactory.instance();
      // El proyecto base llega abierto con -project; solo se abre aqui si no hay uno cargado.
      IProject proyecto = app.getProjectManager().getProject();
      if (proyecto == null || proyecto.toDiagramArray().length == 0) {
        if (!app.getProjectManager().openProject(new File(p.getProperty("entrada"))))
          throw new IOException("No se pudo abrir el proyecto base");
        proyecto = app.getProjectManager().getProject();
      }
      for (IDiagramUIModel d : proyecto.toDiagramArray())
        if (d.getName().startsWith("04") || d.getName().startsWith("05")) {
          for (IShapeUIModel s : d.toShapeUIModelArray()) if (s.getModelElement() != null) s.getModelElement().delete();
          d.delete();
        }

      desarrollo();
      despliegue();

      File vpp = new File(salida, "Jovan-Pets.vpp");
      if (!app.getProjectManager().saveProjectAs(vpp)) throw new IOException("No se guardo el VPP");
      proyecto = app.getProjectManager().getProject();
      IDiagramUIModel[] todos = proyecto.toDiagramArray();
      if (todos.length != 5) throw new IOException("Se esperaban cinco vistas y hay " + todos.length);

      StringBuilder auditoria = new StringBuilder("Generado por Visual Paradigm " + app.getBuildNo() + "\n")
          .append("Fecha: ").append(java.time.ZonedDateTime.now(java.time.ZoneId.of("America/Bogota"))).append("\n");
      for (IDiagramUIModel d : todos) {
        dm.openDiagram(d);
        String n = d.getName();
        String archivo = n.startsWith("01") ? "01-contexto" : n.startsWith("02") ? "02-funcional" : n.startsWith("03") ? "03-logica"
            : n.startsWith("04") ? "04-desarrollo" : "05-despliegue";
        ExportDiagramAsImageOption opt = new ExportDiagramAsImageOption(ExportDiagramAsImageOption.IMAGE_TYPE_PNG);
        opt.setGraphicAntiAliasing(true);
        opt.setTextAntiAliasing(true);
        opt.setScale(1.5f);
        app.getModelConvertionManager().exportDiagramAsImage(d, new File(salida, archivo + ".png"), opt);
        auditoria.append(n).append(" | tipo ").append(d.getType()).append(" | ")
            .append(d.toShapeUIModelArray().length).append(" formas | ")
            .append(d.toConnectorUIModelArray().length).append(" conectores\n");
      }
      auditoria.append("04: diagrama de clases UML por capas (controller, dto, security, service, mapper, repository, document, domain) del modulo de pedidos.\n")
          .append("05: diagrama de despliegue UML con nodos device/executionEnvironment, artefactos y vias de comunicacion.\n")
          .append("Clases, operaciones y paquetes tomados de backend/src/main/java/com/jovanpets.\n");
      app.getProjectManager().saveProject();
      Files.writeString(new File(salida, "validacion-modelo.txt").toPath(), auditoria.toString());
    } catch (Throwable e) {
      try {
        StringWriter s = new StringWriter();
        e.printStackTrace(new PrintWriter(s));
        Files.writeString(new File(carpeta, "error.txt").toPath(), s.toString());
      } catch (Exception ignorado) {}
      throw new RuntimeException(e);
    }
  }

  // ------------------------------------------------------------------ vista de desarrollo

  /** Especificacion de una clase: nombre, estereotipo y miembros en notacion UML. */
  static final class Spec {
    final String nombre, estereotipo;
    final String[] miembros;
    final boolean enumeracion;
    Spec(String nombre, String estereotipo, String[] miembros, boolean enumeracion) {
      this.nombre = nombre; this.estereotipo = estereotipo; this.miembros = miembros; this.enumeracion = enumeracion;
    }
    String nombre() { return nombre; }
    String estereotipo() { return estereotipo; }
    String[] miembros() { return miembros; }
    boolean enumeracion() { return enumeracion; }
  }

  static Spec clase(String nombre, String estereotipo, String... miembros) { return new Spec(nombre, estereotipo, miembros, false); }
  static Spec enumeracion(String nombre, String... literales) { return new Spec(nombre, "enumeration", literales, true); }

  static final class Capa {
    final String nombre, descripcion;
    final Color color;
    final List<Spec> clases;
    Capa(String nombre, String descripcion, Color color, List<Spec> clases) {
      this.nombre = nombre; this.descripcion = descripcion; this.color = color; this.clases = clases;
    }
    String nombre() { return nombre; }
    String descripcion() { return descripcion; }
    Color color() { return color; }
    List<Spec> clases() { return clases; }
  }

  void desarrollo() {
    IDiagramUIModel d = diagrama(DiagramManager.DIAGRAM_TYPE_CLASS_DIAGRAM, "04 · Vista de desarrollo");
    ((IClassDiagramUIModel) d).setShowClassEmptyCompartments(IClassDiagramUIModel.SHOW_CLASS_EMPTY_COMPARTMENTS_NO);

    List<List<Capa>> filas = List.of(
        List.of(
            new Capa("security", "Usuario autenticado (JWT)", GRIS_CAPA, List.of(
                clase("UsuarioAutenticado", "record",
                    "-id : String", "-email : String", "-nombre : String", "-rol : Rol", "-clienteId : String",
                    "+esCliente() : boolean", "+esAdministrador() : boolean"))),
            new Capa("controller", "Capa de presentacion · endpoints REST", AZUL_CAPA, List.of(
                clase("PedidoController", "RestController",
                    "-pedidos : PedidoService",
                    "+cotizar(r : CotizacionRequest) : CotizacionResponse",
                    "+crear(r : PedidoRequest, actor : UsuarioAutenticado) : PedidoResponse",
                    "+listar(estado : EstadoPedido, actor : UsuarioAutenticado) : List<PedidoResponse>",
                    "+obtener(id : String, actor : UsuarioAutenticado) : PedidoResponse",
                    "+cancelar(id : String, r : MotivoRequest, actor : UsuarioAutenticado) : PedidoResponse",
                    "+cambiarEstado(id : String, r : CambioEstadoPedidoRequest, actor : UsuarioAutenticado) : PedidoResponse"))),
            new Capa("dto", "Transferencia de datos · Request / Response validados", MORADO_CAPA, List.of(
                clase("PedidoRequest", "record",
                    "-lineas : List<LineaPedidoRequest>", "-modalidad : ModalidadEntrega", "-direccionEntrega : String", "-clienteId : String"),
                clase("LineaPedidoRequest", "record", "-productoId : String", "-cantidad : Integer"),
                clase("PedidoResponse", "record",
                    "-id : String", "-numero : String", "-clienteNombre : String", "-fecha : LocalDateTime", "-estado : EstadoPedido",
                    "-modalidad : ModalidadEntrega", "-detalles : List<DetallePedidoResponse>", "-total : BigDecimal")))),
        List.of(
            new Capa("service", "Aplicacion y logica de negocio", VERDE_CAPA, List.of(
                clase("PedidoService", "Service",
                    "-pedidos : PedidoRepository", "-productos : ProductoRepository", "-inventario : InventarioService",
                    "-clientes : ClienteService", "-secuencias : SecuenciaRepository", "-reloj : Clock",
                    "+cotizar(lineas : List<LineaPedidoRequest>) : CotizacionResponse",
                    "+crear(r : PedidoRequest, actor : UsuarioAutenticado) : PedidoResponse",
                    "+listar(estado : EstadoPedido, actor : UsuarioAutenticado) : List<PedidoResponse>",
                    "+cambiarEstado(id : String, destino : EstadoPedido, actor : UsuarioAutenticado) : PedidoResponse",
                    "+cancelar(id : String, motivo : String, actor : UsuarioAutenticado) : PedidoResponse",
                    "-cargarVendibles(cantidades : Map<String, Integer>) : Map<String, Producto>",
                    "-verificarAcceso(p : Pedido, actor : UsuarioAutenticado) : void"),
                clase("InventarioService", "Service",
                    "-productos : ProductoRepository", "-movimientos : MovimientoInventarioRepository",
                    "+registrar(r : MovimientoRequest, actor : UsuarioAutenticado) : MovimientoResponse",
                    "+reservar(productoId : String, cantidad : int, nombre : String) : Producto",
                    "+liberar(productoId : String, cantidad : int) : void",
                    "+registrarSalidasPedido(p : Pedido, actualizados : List<Producto>, actor : UsuarioAutenticado) : void",
                    "+reponerPedido(p : Pedido, actor : UsuarioAutenticado) : void",
                    "+alertas() : List<ProductoResponse>"))),
            new Capa("mapper", "Conversion documento a DTO", GRIS_CAPA, List.of(
                clase("PedidoMapper", "utility",
                    "+aResponse(p : Pedido) : PedidoResponse", "+aResponse(d : DetallePedido) : DetallePedidoResponse")))),
        List.of(
            new Capa("repository", "Persistencia · Spring Data MongoDB", NARANJA_CAPA, List.of(
                clase("PedidoRepository", "interface",
                    "+findByClienteIdOrderByFechaDesc(clienteId : String) : List<Pedido>",
                    "+findAllByOrderByFechaDesc() : List<Pedido>", "+findTop5ByOrderByFechaDesc() : List<Pedido>",
                    "+countByEstado(estado : EstadoPedido) : long"),
                clase("ProductoRepository", "interface",
                    "+buscarStockBajo() : List<Producto>", "+findAllByOrderByNombreAsc() : List<Producto>"),
                clase("ProductoStockOperations", "interface",
                    "+descontarStock(productoId : String, cantidad : int) : Optional<Producto>",
                    "+incrementarStock(productoId : String, cantidad : int) : Optional<Producto>",
                    "+buscar(texto : String, categoria : CategoriaProducto, soloActivos : boolean) : List<Producto>"),
                clase("MovimientoInventarioRepository", "interface",
                    "+findByProductoIdOrderByFechaDesc(productoId : String) : List<MovimientoInventario>"),
                clase("SecuenciaRepository", "Repository",
                    "-mongo : MongoTemplate", "+siguiente(nombre : String) : long")))),
        List.of(
            new Capa("document", "Documentos MongoDB (@Document)", AZUL_CAPA, List.of(
                clase("Pedido", "Document",
                    "-id : String", "-version : Long", "-numero : String", "-clienteId : String", "-clienteNombre : String",
                    "-fecha : LocalDateTime", "-estado : EstadoPedido", "-modalidad : ModalidadEntrega",
                    "-direccionEntrega : String", "-total : BigDecimal", "-motivoCancelacion : String"),
                clase("DetallePedido", "embedded",
                    "-productoId : String", "-nombreProducto : String", "-cantidad : int", "-precioUnitario : BigDecimal", "-subtotal : BigDecimal"),
                clase("Producto", "Document",
                    "-id : String", "-nombre : String", "-categoria : CategoriaProducto", "-precio : BigDecimal", "-stock : int",
                    "-stockMinimo : int", "-requiereFormula : boolean", "-activo : boolean", "-version : Long", "+stockBajo() : boolean"),
                clase("MovimientoInventario", "Document",
                    "-id : String", "-productoId : String", "-fecha : LocalDateTime", "-tipo : TipoMovimiento", "-cantidad : int",
                    "-motivo : String", "-saldo : int", "-pedidoId : String"))),
            new Capa("domain", "Reglas y tipos del dominio", VERDE_CAPA, List.of(
                enumeracion("EstadoPedido", "PENDIENTE", "CONFIRMADO", "DESPACHADO", "ENTREGADO", "CANCELADO",
                    "+puedeCambiarA(destino : EstadoPedido) : boolean", "+esCancelable() : boolean"),
                enumeracion("ModalidadEntrega", "DOMICILIO", "RECOGIDA")))));

    // Medidas estimadas de cada clase para distribuir las capas sin solapamientos.
    int y = 180, anchoMaximo = 0;
    List<int[]> marcos = new ArrayList<>();
    List<List<int[]>> posiciones = new ArrayList<>();
    for (List<Capa> fila : filas) {
      int x = 60, altoFila = 0;
      for (Capa capa : fila) {
        int ancho = 30, alto = 0;
        List<int[]> dentro = new ArrayList<>();
        for (Spec s : capa.clases()) {
          int[] m = medir(s);
          dentro.add(new int[] {x + ancho, y + 80, m[0], m[1]});
          ancho += m[0] + 40;
          alto = Math.max(alto, m[1]);
        }
        ancho = Math.max(ancho - 10, 420);
        marcos.add(new int[] {x, y, ancho, alto + 105});
        posiciones.add(dentro);
        x += ancho + 50;
        altoFila = Math.max(altoFila, alto + 105);
      }
      anchoMaximo = Math.max(anchoMaximo, x);
      y += altoFila + 70;
    }

    titulo(d, "Vista de desarrollo", anchoMaximo,
        "Arquitectura en capas del backend Spring Boot · Modulo de pedidos e inventario (los demas modulos siguen la misma estructura)");

    int i = 0;
    for (List<Capa> fila : filas)
      for (Capa capa : fila) {
        int[] r = marcos.get(i);
        IPackage pkg = f.createPackage();
        pkg.setName(capa.nombre());
        pkg.setDocumentation(capa.descripcion() + " · com.jovanpets." + capa.nombre());
        IShapeUIModel marco = forma(d, pkg, capa.nombre(), r[0], r[1], r[2], r[3], capa.color(), 18);
        marco.getElementFont().setBold(true);
        texto(d, capa.descripcion(), r[0] + 20, r[1] + 44, r[2] - 40, 26, 13, capa.color(), false);
        List<int[]> dentro = posiciones.get(i);
        for (int k = 0; k < capa.clases().size(); k++) {
          int[] c = dentro.get(k);
          IShapeUIModel s = crearClase(d, pkg, capa.clases().get(k), c[0], c[1], c[2], c[3]);
          marco.addChild(s);
        }
        i++;
      }

    // Relaciones reales del codigo: atributos inyectados (asociacion dirigida) y usos (dependencias).
    dirigida(d, "PedidoController", "PedidoService", "pedidos");
    usa(d, "PedidoController", "PedidoRequest", "use");
    usa(d, "PedidoController", "PedidoResponse", "use");
    usa(d, "PedidoController", "UsuarioAutenticado", "use");
    dirigida(d, "PedidoService", "PedidoRepository", "pedidos");
    dirigida(d, "PedidoService", "ProductoRepository", "productos");
    dirigida(d, "PedidoService", "SecuenciaRepository", "secuencias");
    dirigida(d, "PedidoService", "InventarioService", "inventario");
    usa(d, "PedidoService", "PedidoMapper", "use");
    usa(d, "PedidoService", "Pedido", "create");
    dirigida(d, "InventarioService", "ProductoRepository", "productos");
    dirigida(d, "InventarioService", "MovimientoInventarioRepository", "movimientos");
    generalizacion(d, "ProductoRepository", "ProductoStockOperations");
    usa(d, "PedidoRepository", "Pedido", "use");
    usa(d, "ProductoRepository", "Producto", "use");
    usa(d, "MovimientoInventarioRepository", "MovimientoInventario", "use");
    usa(d, "PedidoMapper", "PedidoResponse", "create");
    composicion(d, "Pedido", "DetallePedido");
    usa(d, "Pedido", "EstadoPedido", "use");

    texto(d,
        "Reglas de la arquitectura en capas: controller -> service -> repository -> MongoDB, sin dependencias circulares. Los controllers no contienen logica de negocio;\n"
            + "los services centralizan las reglas (stock atomico, compensacion, estados) y los repositories el acceso a datos. La API expone DTO, nunca documentos.\n"
            + "Repositorios de interfaz: extienden MongoRepository<T, String>. Paquetes transversales no detallados: config (SecurityConfig, AgendaConfig, DatosIniciales),\n"
            + "exception (GlobalExceptionHandler, NegocioException) y frontend React (pages, components, services/api.ts) que consume /api/** por HTTP.",
        60, y, Math.max(anchoMaximo - 120, 1200), 110, 16, BLANCO, false);
    dm.openDiagram(d);
  }

  int[] medir(Spec s) {
    int largo = s.nombre().length() + 6;
    for (String m : s.miembros()) largo = Math.max(largo, m.length());
    int lineas = s.miembros().length;
    int alto = 52 + lineas * 16 + (s.enumeracion() ? 14 : 8);
    return new int[] {Math.min(Math.max(170, (int) (largo * 6.9) + 24), 720), alto};
  }

  IShapeUIModel crearClase(IDiagramUIModel d, IPackage pkg, Spec s, int x, int y, int w, int h) {
    IClass c = f.createClass();
    c.setName(s.nombre());
    pkg.addChild(c);
    if (s.estereotipo() != null) c.addStereotype(s.estereotipo());
    for (String m : s.miembros()) {
      if (s.enumeracion() && !m.contains("(")) {
        c.createEnumerationLiteral().setName(m);
        continue;
      }
      String visibilidad = m.startsWith("-") ? IAttribute.VISIBILITY_PRIVATE : IAttribute.VISIBILITY_PUBLIC;
      String cuerpo = m.substring(1);
      if (cuerpo.contains("(")) {
        IOperation op = c.createOperation();
        op.setVisibility(visibilidad);
        op.setName(cuerpo.substring(0, cuerpo.indexOf('(')));
        String params = cuerpo.substring(cuerpo.indexOf('(') + 1, cuerpo.lastIndexOf(')'));
        String retorno = cuerpo.substring(cuerpo.lastIndexOf(')') + 1).replaceFirst("^\\s*:\\s*", "").trim();
        op.setReturnType(retorno);
        for (String p : dividirParametros(params)) {
          String[] partes = p.split(" : ", 2);
          IParameter par = op.createParameter();
          par.setName(partes[0].trim());
          par.setType(partes.length > 1 ? partes[1].trim() : "");
        }
      } else {
        String[] partes = cuerpo.split(" : ", 2);
        IAttribute a = c.createAttribute();
        a.setVisibility(visibilidad);
        a.setName(partes[0].trim());
        a.setType(partes.length > 1 ? partes[1].trim() : "");
      }
    }
    c.setDocumentation("Clase real: com.jovanpets." + pkg.getName() + "." + s.nombre());
    IClassUIModel u = (IClassUIModel) forma(d, c, s.nombre(), x, y, w, h, BLANCO, FUENTE);
    u.setShowAttributeType(IClassUIModel.ATTR_SHOW_TYPE_ALL);
    u.setVisibilityStyle(IClassUIModel.VS_UML);
    u.setShowEmptyCompartments(IClassDiagramUIModel.SHOW_CLASS_EMPTY_COMPARTMENTS_NO);
    u.setShowOperationSignature(true);
    u.setShowParameterNameInOperationSignature(true);
    formas.put(s.nombre(), u);
    return u;
  }

  /** Divide parametros respetando los genericos (Map<String, Integer>). */
  static List<String> dividirParametros(String texto) {
    List<String> res = new ArrayList<>();
    int nivel = 0, inicio = 0;
    for (int k = 0; k < texto.length(); k++) {
      char ch = texto.charAt(k);
      if (ch == '<') nivel++;
      else if (ch == '>') nivel--;
      else if (ch == ',' && nivel == 0) {
        res.add(texto.substring(inicio, k).trim());
        inicio = k + 1;
      }
    }
    if (!texto.isBlank()) res.add(texto.substring(inicio).trim());
    return res;
  }

  IConnectorUIModel conectar(IDiagramUIModel d, IModelElement r, String desde, String hasta) {
    IConnectorUIModel c = (IConnectorUIModel) dm.createConnector(d, r, formas.get(desde), formas.get(hasta), null);
    c.setForeground(NEGRO);
    c.getElementFont().setValues("Arial", false, false, 13, NEGRO);
    return c;
  }

  void usa(IDiagramUIModel d, String desde, String hasta, String estereotipo) {
    IDependency r = f.createDependency();
    r.setFrom(formas.get(desde).getModelElement());
    r.setTo(formas.get(hasta).getModelElement());
    r.addStereotype(estereotipo);
    conectar(d, r, desde, hasta);
  }

  void dirigida(IDiagramUIModel d, String desde, String hasta, String rol) {
    IAssociation r = f.createAssociation();
    r.setFrom(formas.get(desde).getModelElement());
    r.setTo(formas.get(hasta).getModelElement());
    IAssociationEnd origen = (IAssociationEnd) r.getFromEnd(), destino = (IAssociationEnd) r.getToEnd();
    origen.setNavigable(IAssociationEnd.NAVIGABLE_NAV_NON_NAVIGABLE);
    destino.setNavigable(IAssociationEnd.NAVIGABLE_NAV_NAVIGABLE);
    destino.setMultiplicity("1");
    destino.setName(rol);
    conectar(d, r, desde, hasta);
  }

  void composicion(IDiagramUIModel d, String todo, String parte) {
    IAssociation r = f.createAssociation();
    r.setFrom(formas.get(todo).getModelElement());
    r.setTo(formas.get(parte).getModelElement());
    ((IAssociationEnd) r.getFromEnd()).setAggregationKind(IAssociationEnd.AGGREGATION_KIND_COMPOSITED);
    ((IAssociationEnd) r.getFromEnd()).setMultiplicity("1");
    ((IAssociationEnd) r.getToEnd()).setMultiplicity("1..*");
    ((IAssociationEnd) r.getToEnd()).setName("detalles");
    conectar(d, r, todo, parte);
  }

  void generalizacion(IDiagramUIModel d, String hija, String padre) {
    IGeneralization g = f.createGeneralization();
    g.setFrom(formas.get(padre).getModelElement());
    g.setTo(formas.get(hija).getModelElement());
    conectar(d, g, padre, hija);
  }

  // ------------------------------------------------------------------ vista de despliegue

  IShapeUIModel nodo(IDiagramUIModel d, IModelElement padre, IShapeUIModel formaPadre, String estereotipo, String nombre,
      int x, int y, int w, int h, Color color, int tam) {
    INode n = f.createNode();
    if (padre instanceof INode) ((INode) padre).addNode(n);
    n.addStereotype(estereotipo);
    IShapeUIModel s = forma(d, n, nombre, x, y, w, h, color, tam);
    s.getElementFont().setBold(true);
    if (formaPadre != null) formaPadre.addChild(s);
    return s;
  }

  IShapeUIModel artefacto(IDiagramUIModel d, IShapeUIModel formaPadre, String nombre, String doc, int x, int y, int w, int h) {
    IArtifact a = f.createArtifact();
    ((INode) formaPadre.getModelElement()).addArtifact(a);
    a.setDocumentation(doc);
    IShapeUIModel s = forma(d, a, nombre, x, y, w, h, BLANCO, 15);
    formaPadre.addChild(s);
    return s;
  }

  void via(IDiagramUIModel d, IShapeUIModel a, IShapeUIModel b, String nombre, String protocolo, int[][] puntos, int x, int y, int w, int h) {
    IAssociation r = f.createAssociation();
    r.setFrom(a.getModelElement());
    r.setTo(b.getModelElement());
    r.setName(nombre);
    r.addStereotype(protocolo);
    Point[] ps = puntos == null ? null : Arrays.stream(puntos).map(p -> new Point(p[0], p[1])).toArray(Point[]::new);
    IConnectorUIModel c = (IConnectorUIModel) dm.createConnector(d, r, a, b, ps);
    c.setForeground(NEGRO);
    c.getElementFont().setValues("Arial", false, false, 15, NEGRO);
    c.setRequestResetCaption(false);
    c.getCaptionUIModel().setSide(ICaptionUIModel.SIDE_FREEMOVE);
    c.getCaptionUIModel().setBounds(x, y, w, h);
  }

  void despliegue() {
    IDiagramUIModel d = diagrama(DiagramManager.DIAGRAM_TYPE_DEPLOYMENT, "05 · Vista de despliegue");
    titulo(d, "Vista de despliegue", 2000,
        "Topologia de ejecucion de Jovan Pets · Navegador, frontend React, backend Spring Boot y MongoDB");

    // Dispositivo del usuario
    IShapeUIModel equipo = nodo(d, null, null, "device", ":Equipo del usuario\n(PC, tableta o movil)", 60, 200, 560, 470, new Color(250, 250, 250), 18);
    IShapeUIModel navegador = nodo(d, equipo.getModelElement(), equipo, "executionEnvironment", ":Navegador web\n(Chrome, Edge, Firefox, Safari)", 95, 290, 490, 350, BLANCO, 16);
    artefacto(d, navegador, "Jovan Pets SPA\n(React 18 + TypeScript)\nindex.html · assets/*.js · *.css",
        "Aplicacion de una sola pagina compilada con Vite. Guarda el token JWT en sessionStorage y el carrito en localStorage.", 130, 400, 420, 120);
    texto(d, "Rutas protegidas por rol · token Bearer\nrenovado mientras hay actividad", 130, 540, 420, 60, 14, BLANCO, false);

    // Servidor del frontend
    IShapeUIModel web = nodo(d, null, null, "device", ":Servidor web del frontend", 60, 800, 560, 330, new Color(250, 250, 250), 18);
    IShapeUIModel estaticos = nodo(d, web.getModelElement(), web, "executionEnvironment", ":Servidor de archivos estaticos\nDesarrollo: Vite dev server :5173", 95, 880, 490, 220, BLANCO, 16);
    artefacto(d, estaticos, "frontend/dist\n(build de produccion)", "Resultado de npm run build. Configuracion: VITE_API_URL.", 130, 980, 420, 90);

    // Servidor de aplicacion
    IShapeUIModel servidor = nodo(d, null, null, "device", ":Servidor de aplicacion", 780, 200, 620, 930, new Color(250, 250, 250), 18);
    IShapeUIModel jvm = nodo(d, servidor.getModelElement(), servidor, "executionEnvironment", ":JVM Java 21", 815, 290, 550, 800, BLANCO, 16);
    IShapeUIModel boot = nodo(d, jvm.getModelElement(), jvm, "executionEnvironment", ":Spring Boot 3.5\nTomcat embebido · puerto 8080", 845, 380, 490, 680, AZUL_CAPA, 16);
    artefacto(d, boot, "jovan-pets-backend-1.0.0.jar",
        "JAR ejecutable generado con mvnw package. Contiene la API REST y la logica de negocio.", 880, 480, 420, 70);
    texto(d, "API REST /api/** (controllers + DTO)\nSeguridad: Spring Security + JWT (HS256)\nServicios y reglas de negocio\nRepositorios Spring Data MongoDB\nDriver MongoDB para Java",
        880, 570, 420, 140, 15, AZUL_CAPA, false);
    IShapeUIModel config = artefacto(d, boot, "application.properties\n+ variables de entorno (.env)",
        "MONGODB_URI, JWT_SECRET, CORS_ORIGENES, FRONTEND_URL, ADMIN_EMAIL, ADMIN_PASSWORD, APP_DATOS_INICIALES", 880, 740, 420, 90);
    ((IModelElement) config.getModelElement()).addStereotype("deployment spec");

    // Servidor de base de datos
    IShapeUIModel bd = nodo(d, null, null, "device", ":Servidor de base de datos", 1560, 200, 420, 930, new Color(250, 250, 250), 18);
    IShapeUIModel mongo = nodo(d, bd.getModelElement(), bd, "executionEnvironment", ":MongoDB 7.0\nmongod · puerto 27017", 1595, 290, 350, 800, NARANJA_CAPA, 16);
    artefacto(d, mongo, "Base de datos jovanpets",
        "Colecciones e indices creados por Spring Data MongoDB al iniciar.", 1625, 390, 290, 70);
    texto(d, "Colecciones:\nusuarios · clientes\nmascotas · atenciones\nservicios · citas\nproductos\nmovimientos_inventario\npedidos · contadores\n\nIndices unicos: email,\ncedula, numero de pedido,\n(agenda, franjas) parcial",
        1625, 480, 290, 330, 15, NARANJA_CAPA, false);

    via(d, navegador, estaticos, "Descarga de la SPA", "HTTP/HTTPS",
        new int[][] {{340, 640}, {340, 880}}, 355, 715, 260, 60);
    via(d, navegador, boot, "Peticiones REST JSON\nAuthorization: Bearer JWT", "HTTP/HTTPS :8080",
        new int[][] {{585, 450}, {845, 450}}, 600, 330, 245, 110);
    via(d, boot, mongo, "MongoDB Wire Protocol\n(driver Java)", "TCP :27017",
        new int[][] {{1335, 640}, {1595, 640}}, 1350, 545, 240, 90);

    texto(d,
        "Recorrido de una solicitud: (1) el usuario interactua con la SPA en el navegador -> (2) la SPA envia una peticion HTTP(S) con el token JWT al backend -> (3) Spring Security valida el token y el\n"
            + "controller delega al service, que aplica las reglas de negocio -> (4) los repositories consultan o modifican MongoDB mediante el driver -> (5) el backend responde JSON y la SPA actualiza la vista.\n"
            + "Desarrollo local: los tres nodos corren en localhost (Vite :5173, Spring Boot :8080, MongoDB :27017). Despliegue final: misma topologia con HTTPS; el proveedor de hosting aun no esta seleccionado.",
        60, 1170, 1920, 110, 16, BLANCO, false);
    dm.openDiagram(d);
  }
}
