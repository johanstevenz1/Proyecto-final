import com.vp.plugin.*;
import com.vp.plugin.diagram.*;
import com.vp.plugin.diagram.shape.IClassUIModel;
import com.vp.plugin.diagram.shape.IStructuredInterfaceUIModel;
import com.vp.plugin.model.*;
import com.vp.plugin.model.factory.IModelElementFactory;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.List;

/**
 * Regenera las cinco vistas de Jovan Pets aplicando las observaciones del profesor:
 * <ul>
 *   <li>01 Contexto: los flujos se analizan por entidad; el servicio de correo solo recibe datos.
 *   <li>02 Funcional: se muestran las partes del sistema y como se conectan entre ellas (interfaces
 *       provistas y requeridas), sin un componente central que represente al sistema completo.
 *   <li>03 Logica: clases con atributos, constructores, getters, setters y operaciones extraidos del
 *       codigo, con herencia, realizacion de interfaz, agregacion, composicion, asociaciones y
 *       dependencias.
 *   <li>04 Desarrollo: arquitectura por capas (sin cambios de criterio).
 *   <li>05 Despliegue: topologia publicada (Vercel, Render y MongoDB Atlas) y entorno local.
 * </ul>
 */
public class VistasCorregidas extends VistasDesarrolloDespliegue {
  static final Color AZUL_SUAVE = new Color(227, 242, 253), VERDE_SUAVE = new Color(232, 245, 233);
  static final Color MORADO_SUAVE = new Color(243, 229, 245), NARANJA_SUAVE = new Color(255, 243, 224);
  static final Color GRIS_SUAVE = new Color(245, 245, 245);

  File modelo;

  @Override
  public void invoke(String[] argumentos) {
    try {
      Properties p = new Properties();
      try (InputStream in = new FileInputStream(new File(carpeta, "salida.properties"))) { p.load(in); }
      File salida = new File(p.getProperty("salida"));
      salida.mkdirs();
      modelo = new File(p.getProperty("modelo"));
      app = ApplicationManager.instance();
      dm = app.getDiagramManager();
      f = IModelElementFactory.instance();

      IProject proyecto = app.getProjectManager().getProject();
      for (IDiagramUIModel d : proyecto.toDiagramArray()) {
        for (IConnectorUIModel c : d.toConnectorUIModelArray()) borrar(c.getModelElement());
        for (IShapeUIModel s : d.toShapeUIModelArray()) borrar(s.getModelElement());
        d.delete();
      }

      contexto();
      componentes();
      logica();
      desarrollo();
      despliegue();

      File vpp = new File(salida, "Jovan-Pets.vpp");
      if (!app.getProjectManager().saveProjectAs(vpp)) throw new IOException("No se guardo el VPP");
      IDiagramUIModel[] todos = app.getProjectManager().getProject().toDiagramArray();
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
        opt.setScale(n.startsWith("03") ? 1.2f : 1.5f);
        app.getModelConvertionManager().exportDiagramAsImage(d, new File(salida, archivo + ".png"), opt);
        auditoria.append(n).append(" | tipo ").append(d.getType()).append(" | ")
            .append(d.toShapeUIModelArray().length).append(" formas | ")
            .append(d.toConnectorUIModelArray().length).append(" conectores\n");
      }
      auditoria.append(conteos);
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

  final StringBuilder conteos = new StringBuilder();

  static void borrar(IModelElement m) {
    try {
      if (m != null) m.delete();
    } catch (RuntimeException ignorado) {
      // ya eliminado junto con su contenedor
    }
  }

  /** Mueve el titulo y las notas alrededor del contenido ya distribuido por el auto-layout. */
  int[] limites(IDiagramUIModel d) {
    int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, maxX = 0, maxY = 0;
    for (IShapeUIModel s : d.toShapeUIModelArray()) {
      if (s.getParent() != null) continue;
      minX = Math.min(minX, s.getX());
      minY = Math.min(minY, s.getY());
      maxX = Math.max(maxX, s.getX() + s.getWidth());
      maxY = Math.max(maxY, s.getY() + s.getHeight());
    }
    return new int[] {minX, minY, maxX, maxY};
  }

  // ================================================================ 01 · Vista de contexto

  @Override
  void contexto() {
    IDiagramUIModel d = diagrama("YourdonDeMarcoDFD", "01 · Vista de contexto");
    titulo(d, "Vista de contexto", 1600, "Jovan Pets · Un unico sistema y cuatro entidades externas · HU-01 a HU-24");
    IYdDfdProcess proceso = f.createYdDfdProcess();
    proceso.setDfId("0");
    IShapeUIModel sistema = forma(d, proceso, "Jovan Pets", 675, 400, 250, 250, Color.WHITE, 25);
    IShapeUIModel cliente = forma(d, f.createYdDfdExternalEntity(), "Cliente", 660, 190, 280, 70, Color.WHITE, 22);
    IShapeUIModel admin = forma(d, f.createYdDfdExternalEntity(), "Administrador", 70, 470, 280, 100, Color.WHITE, 22);
    IShapeUIModel personal = forma(d, f.createYdDfdExternalEntity(), "Personal veterinario", 1250, 470, 280, 100, Color.WHITE, 22);
    IShapeUIModel correo = forma(d, f.createYdDfdExternalEntity(), "Servicio de correo\n(integracion pendiente)", 625, 820, 350, 90, new Color(245, 245, 245), 20);
    flujo(d, cliente, sistema, "Datos de cuenta, mascotas,\nsolicitudes de cita y pedidos", new int[][] {{720, 260}, {720, 400}}, 440, 295, 265, 75);
    flujo(d, sistema, cliente, "Catalogo, horarios, historial\ny estados de citas y pedidos", new int[][] {{870, 400}, {870, 260}}, 900, 295, 280, 75);
    flujo(d, admin, sistema, "Clientes, servicios, productos,\nprecios y roles de usuario", new int[][] {{350, 495}, {675, 495}}, 360, 420, 300, 65);
    flujo(d, sistema, admin, "Listados, confirmaciones\ny metricas del negocio", new int[][] {{675, 550}, {350, 550}}, 370, 560, 290, 65);
    flujo(d, personal, sistema, "Confirmacion de citas, atenciones\nclinicas y movimientos de stock", new int[][] {{1250, 550}, {925, 550}}, 935, 560, 310, 65);
    flujo(d, sistema, personal, "Agenda, historial clinico\ny alertas de stock", new int[][] {{925, 495}, {1250, 495}}, 940, 420, 290, 65);
    // El servicio de correo no envia datos al sistema: solo recibe el enlace a entregar (HU-22).
    flujo(d, sistema, correo, "Enlace temporal de recuperacion\nde contrasena [HU-22]", new int[][] {{800, 650}, {800, 820}}, 820, 705, 300, 65);
    texto(d,
        "Limite: gestion veterinaria y comercial. Sin pagos electronicos ni sistemas externos de reparto.\n"
            + "Los flujos se analizan por entidad: Cliente, Administrador y Personal veterinario envian y reciben datos;\n"
            + "el Servicio de correo solo recibe del sistema (no le envia informacion). Las flechas representan datos, no modulos internos.",
        60, 960, 1480, 110, 18, Color.WHITE, false);
    dm.openDiagram(d);
  }

  // ================================================================ 02 · Vista funcional

  final Map<String, IShapeUIModel> partes = new LinkedHashMap<>();
  final Map<String, IShapeUIModel> interfaces = new LinkedHashMap<>();
  final Map<String, Integer> salidasArriba = new HashMap<>();
  final Map<String, Integer> salidasAbajo = new HashMap<>();
  static final int ANCHO_PARTE = 300, ALTO_PARTE = 96, FILA = 900, BUS_API = 540;
  int carril = 580;

  IShapeUIModel parte(IDiagramUIModel d, String clave, String nombre, String clases, Color color, int x, int y) {
    IComponent c = f.createComponent();
    c.setDocumentation(clases);
    IShapeUIModel s = forma(d, c, nombre, x, y, ANCHO_PARTE, ALTO_PARTE, color, 17);
    s.getElementFont().setBold(true);
    partes.put(clave, s);
    return s;
  }

  int centroX(IShapeUIModel s) { return s.getX() + s.getWidth() / 2; }

  /** La parte provee una interfaz (circulo) ubicada encima de ella, unida por una realizacion. */
  void provee(IDiagramUIModel d, String parte, String interfaz, String descripcion) {
    IShapeUIModel p = partes.get(parte);
    IClass i = f.createClass();
    i.setName(interfaz);
    i.addStereotype("Interface");
    i.setDocumentation(descripcion);
    IStructuredInterfaceUIModel vista =
        (IStructuredInterfaceUIModel) dm.createDiagramElement(d, IShapeTypeConstants.SHAPE_TYPE_STRUCTURED_INTERFACE);
    vista.setModelElement(i);
    int cx = centroX(p), cy = p.getY() - 55;
    vista.setBounds(cx - 13, cy - 13, 26, 26);
    vista.setBackground(Color.WHITE);
    vista.getFillColor().setType(0);
    vista.getFillColor().setColor1(Color.WHITE);
    vista.getElementFont().setValues("Arial", false, true, 15, NEGRO);
    vista.getLineModel().setColor(NEGRO);
    vista.setRequestResetCaption(false);
    vista.getCaptionUIModel().setSide(ICaptionUIModel.SIDE_FREEMOVE);
    vista.getCaptionUIModel().setBounds(cx + 16, cy - 34, 200, 24);
    interfaces.put(interfaz, vista);
    IRealization r = f.createRealization();
    r.setFrom(p.getModelElement());
    r.setTo(i);
    IConnectorUIModel c = (IConnectorUIModel) dm.createConnector(d, r, p, vista,
        new Point[] {new Point(cx, p.getY()), new Point(cx, cy + 13)});
    c.getLineModel().setColor(NEGRO);
    c.getCaptionUIModel().setVisible(false);
  }

  IConnectorUIModel uso(IDiagramUIModel d, String parte, String interfaz, Point[] puntos, Color color) {
    IUsage u = f.createUsage();
    u.setFrom(partes.get(parte).getModelElement());
    u.setTo(interfaces.get(interfaz).getModelElement());
    IConnectorUIModel c = (IConnectorUIModel) dm.createConnector(d, u, partes.get(parte), interfaces.get(interfaz), puntos);
    c.setConnectorStyle(IConnectorUIModel.CS_RECTI_LINEAR);
    c.getLineModel().setColor(color);
    c.getCaptionUIModel().setVisible(false);
    return c;
  }

  /** Requiere una interfaz de otra parte de la misma fila: sale por arriba y usa su propio carril. */
  void requiere(IDiagramUIModel d, String parte, String interfaz) {
    IShapeUIModel p = partes.get(parte);
    IShapeUIModel i = interfaces.get(interfaz);
    int k = salidasArriba.merge(parte, 1, Integer::sum);
    int x0 = p.getX() + 30 + (k - 1) * 28, ix = i.getX() + 13, y = carril;
    carril += 17;
    uso(d, parte, interfaz, new Point[] {new Point(x0, p.getY()), new Point(x0, y), new Point(ix, y), new Point(ix, i.getY())},
        new Color(40, 90, 160));
  }

  /** La API (arriba) requiere la interfaz de una parte de la fila: baja por el bus de distribucion. */
  void requiereDesdeArriba(IDiagramUIModel d, String parte, String interfaz, int desplazamiento) {
    IShapeUIModel p = partes.get(parte);
    IShapeUIModel i = interfaces.get(interfaz);
    int x0 = centroX(p) + desplazamiento, ix = i.getX() + 13;
    uso(d, parte, interfaz, new Point[] {new Point(x0, p.getY() + p.getHeight()), new Point(x0, BUS_API), new Point(ix, BUS_API), new Point(ix, i.getY())},
        new Color(90, 90, 90));
  }

  /** Una parte de la fila requiere una interfaz de una parte ubicada debajo (acceso a datos). */
  void requiereHaciaAbajo(IDiagramUIModel d, String parte, String interfaz, int bus) {
    IShapeUIModel p = partes.get(parte);
    IShapeUIModel i = interfaces.get(interfaz);
    int k = salidasAbajo.merge(parte, 1, Integer::sum);
    int x0 = centroX(p) + (k - 1) * 20, ix = i.getX() + 13;
    uso(d, parte, interfaz, new Point[] {new Point(x0, p.getY() + p.getHeight()), new Point(x0, bus), new Point(ix, bus), new Point(ix, i.getY())},
        new Color(120, 120, 120));
  }

  @Override
  void componentes() {
    IDiagramUIModel d = diagrama(DiagramManager.DIAGRAM_TYPE_COMPONENT_DIAGRAM, "02 · Vista funcional");
    String[][] fila = {
      {"clientes", "Clientes", "ClienteService"},
      {"mascotas", "Mascotas e historial\nclinico", "MascotaService, AtencionService"},
      {"servicios", "Catalogo de servicios\n(veterinaria y peluqueria)", "ServicioCatalogoService"},
      {"citas", "Citas y agenda", "CitaService, domain/HorarioAtencion"},
      {"inventario", "Inventario", "InventarioService"},
      {"productos", "Productos", "ProductoService"},
      {"pedidos", "Pedidos", "PedidoService"},
      {"panel", "Panel administrativo", "PanelService"},
      {"usuarios", "Usuarios y acceso", "AuthService, UsuarioService"},
      {"correo", "Correo (pendiente)", "ServicioCorreo, CorreoRegistroLocalService"},
    };
    int paso = ANCHO_PARTE + 60, x = 60;
    for (String[] p : fila) {
      Color color = p[0].equals("correo") ? GRIS_SUAVE : List.of("inventario", "productos", "pedidos").contains(p[0]) ? NARANJA_SUAVE
          : p[0].equals("panel") ? MORADO_SUAVE : VERDE_SUAVE;
      parte(d, p[0], p[1], p[2], color, x, FILA);
      x += paso;
    }
    int ancho = x;
    int centro = ancho / 2 - ANCHO_PARTE / 2;
    parte(d, "front", "Frontend web\n(React + TypeScript)", "frontend/src: pages, components, services/api.ts", AZUL_SUAVE, centro, 200);
    parte(d, "api", "API REST y seguridad\n(controllers + JWT)", "controller/*Controller, security/ConvertidorJwt, TokenService, config/SecurityConfig", AZUL_SUAVE, centro, 380);
    parte(d, "datos", "Acceso a datos\n(Spring Data MongoDB)", "repository/*Repository, ProductoStockOperations, SecuenciaRepository", GRIS_SUAVE, centro, 1240);
    IShapeUIModel mongo = parte(d, "mongo", "MongoDB Atlas", "Base de datos jovanpets", GRIS_SUAVE, centro, 1460);
    ((IComponent) mongo.getModelElement()).addStereotype("database");

    provee(d, "api", "IApiRest", "HTTP/JSON con token Bearer: /api/**");
    provee(d, "clientes", "IClientes", "Fichas de clientes: obtenerActivo, nombres, buscar");
    provee(d, "mascotas", "IMascotas", "Mascotas, verificarAcceso e historial clinico");
    provee(d, "servicios", "IServicios", "Servicios activos: obtenerActivo");
    provee(d, "citas", "ICitas", "Disponibilidad, solicitud y gestion de citas");
    provee(d, "inventario", "IInventario", "reservar, liberar, registrarSalidasPedido, reponerPedido, alertas");
    provee(d, "productos", "IProductos", "Catalogo y administracion de productos");
    provee(d, "pedidos", "IPedidos", "Cotizacion, creacion y estados de pedidos");
    provee(d, "panel", "IPanel", "Metricas del dashboard");
    provee(d, "usuarios", "IUsuarios", "Registro, sesion, recuperacion de contrasena y roles");
    provee(d, "correo", "ServicioCorreo", "enviarRecuperacion(destino, nombre, enlace)");
    provee(d, "datos", "IRepositorios", "MongoRepository y operaciones atomicas de stock");
    provee(d, "mongo", "Protocolo MongoDB", "mongodb+srv sobre TLS");

    // Frontend -> API
    IShapeUIModel front = partes.get("front"), api = partes.get("api");
    uso(d, "front", "IApiRest", new Point[] {new Point(centroX(front), front.getY() + front.getHeight()), new Point(centroX(front), interfaces.get("IApiRest").getY())},
        NEGRO);
    // API -> cada parte de negocio por el bus de distribucion
    List<String> deApi = List.of("IClientes", "IMascotas", "IServicios", "ICitas", "IInventario", "IProductos", "IPedidos", "IPanel", "IUsuarios");
    for (int k = 0; k < deApi.size(); k++) requiereDesdeArriba(d, "api", deApi.get(k), (k - deApi.size() / 2) * 22);

    // Conexiones entre partes (dependencias reales por constructor)
    requiere(d, "mascotas", "IClientes");
    requiere(d, "citas", "IMascotas");
    requiere(d, "citas", "IClientes");
    requiere(d, "citas", "IServicios");
    requiere(d, "productos", "IInventario");
    requiere(d, "pedidos", "IInventario");
    requiere(d, "pedidos", "IClientes");
    for (String i : List.of("IClientes", "IMascotas", "ICitas", "IInventario", "IPedidos")) requiere(d, "panel", i);
    requiere(d, "usuarios", "ServicioCorreo");

    // Persistencia
    for (String p : List.of("clientes", "mascotas", "servicios", "citas", "inventario", "productos", "pedidos", "panel", "usuarios"))
      requiereHaciaAbajo(d, p, "IRepositorios", 1110);
    IShapeUIModel datos = partes.get("datos");
    uso(d, "datos", "Protocolo MongoDB", new Point[] {new Point(centroX(datos), datos.getY() + datos.getHeight()), new Point(centroX(datos), interfaces.get("Protocolo MongoDB").getY())},
        NEGRO);

    texto(d, "Vista funcional: componentes", 40, 20, ancho - 40, 80, 32, AMARILLO, true);
    texto(d, "Partes de Jovan Pets y como se conectan entre ellas · Circulo: interfaz provista (realizacion) · Flecha punteada: interfaz requerida (uso)",
        40, 112, ancho - 40, 40, 17, Color.WHITE, false);
    texto(d,
        "Cada parte depende solo de las interfaces de las partes que usa en el codigo (inyeccion por constructor). Lineas azules: conexiones entre partes de negocio;\n"
            + "lineas grises superiores: la API REST delega en cada parte; lineas grises inferiores: persistencia mediante IRepositorios. No hay un componente central que represente al sistema.",
        40, 1600, ancho - 40, 70, 16, Color.WHITE, false);
    conteos.append("02: ").append(partes.size()).append(" componentes, ").append(interfaces.size())
        .append(" interfaces provistas, conexiones parte a parte mediante Realization/Usage; sin componente central del sistema.\n");
    dm.openDiagram(d);
  }

  // ================================================================ 03 · Vista logica

  final Map<String, IClassUIModel> clases = new LinkedHashMap<>();

  void cargarClases(IDiagramUIModel d) throws IOException {
    List<String[]> definiciones = new ArrayList<>();
    for (String linea : Files.readAllLines(modelo.toPath(), StandardCharsets.UTF_8)) {
      if (linea.isBlank()) continue;
      if (linea.startsWith("CLASE|")) {
        definiciones.add(linea.split("\\|", -1));
      } else {
        definiciones.get(definiciones.size() - 1)[0] += "\n" + linea;
      }
    }
    for (String[] def : definiciones) {
      String[] cuerpo = def[0].split("\n");
      String nombreCompleto = def[1];
      String simple = nombreCompleto.contains("<") ? nombreCompleto.substring(0, nombreCompleto.indexOf('<')) : nombreCompleto;
      String estereotipo = def[2];
      boolean abstracta = "abstracta".equals(def[3]);
      IClass c = f.createClass();
      c.setName(nombreCompleto);
      c.setDocumentation("Clase real: " + def[4] + "." + simple);
      if (!estereotipo.isEmpty()) c.addStereotype(estereotipo);
      if (abstracta) c.setAbstract(true);
      int lineas = 0, largo = nombreCompleto.length() + 4;
      for (int k = 1; k < cuerpo.length; k++) {
        String m = cuerpo[k];
        lineas++;
        largo = Math.max(largo, m.length());
        if (m.startsWith("LITERAL|")) {
          c.createEnumerationLiteral().setName(m.substring(8));
          continue;
        }
        String vis = m.startsWith("-") ? IAttribute.VISIBILITY_PRIVATE : m.startsWith("#") ? IAttribute.VISIBILITY_PROTECTED : IAttribute.VISIBILITY_PUBLIC;
        String resto = m.substring(1);
        if (resto.contains("(")) {
          IOperation op = c.createOperation();
          op.setVisibility(vis);
          op.setName(resto.substring(0, resto.indexOf('(')));
          String params = resto.substring(resto.indexOf('(') + 1, resto.lastIndexOf(')'));
          String ret = resto.substring(resto.lastIndexOf(')') + 1).replaceFirst("^\\s*:\\s*", "").trim();
          if (!ret.isEmpty()) op.setReturnType(ret);
          if ("interface".equals(estereotipo) && "siguientes".equals(op.getName())) op.setAbstract(true);
          for (String par : dividirParametros(params)) {
            String[] partesPar = par.split(" : ", 2);
            IParameter pm = op.createParameter();
            pm.setName(partesPar[0].trim());
            pm.setType(partesPar.length > 1 ? partesPar[1].trim() : "");
          }
        } else {
          String[] partesAt = resto.split(" : ", 2);
          IAttribute a = c.createAttribute();
          a.setVisibility(vis);
          a.setName(partesAt[0].trim());
          a.setType(partesAt.length > 1 ? partesAt[1].trim() : "");
        }
      }
      int w = Math.min(Math.max(180, (int) (largo * 6.6) + 24), 560);
      int h = 46 + lineas * 15 + 18;
      IClassUIModel u = (IClassUIModel) forma(d, c, nombreCompleto, 0, 0, w, h, colorClase(estereotipo, abstracta), 12);
      u.setShowAttributeType(IClassUIModel.ATTR_SHOW_TYPE_ALL);
      u.setVisibilityStyle(IClassUIModel.VS_UML);
      u.setShowOperationSignature(true);
      u.setShowParameterNameInOperationSignature(true);
      clases.put(simple, u);
    }
  }

  static Color colorClase(String estereotipo, boolean abstracta) {
    if ("enumeration".equals(estereotipo)) return NARANJA_SUAVE;
    if ("interface".equals(estereotipo)) return MORADO_SUAVE;
    if (abstracta) return GRIS_SUAVE;
    return VERDE_SUAVE;
  }

  Point[] ruta;

  IConnectorUIModel unir(IDiagramUIModel d, IModelElement r, String a, String b) {
    IConnectorUIModel c = (IConnectorUIModel) dm.createConnector(d, r, clases.get(a), clases.get(b), ruta);
    if (ruta != null) c.setConnectorStyle(IConnectorUIModel.CS_RECTI_LINEAR);
    ruta = null;
    c.setForeground(NEGRO);
    c.getElementFont().setValues("Arial", false, false, 12, NEGRO);
    return c;
  }

  void hereda(IDiagramUIModel d, String hija, String padre) {
    IGeneralization g = f.createGeneralization();
    g.setFrom(clases.get(padre).getModelElement());
    g.setTo(clases.get(hija).getModelElement());
    unir(d, g, padre, hija);
  }

  void realiza(IDiagramUIModel d, String clase, String interfaz) {
    IRealization r = f.createRealization();
    r.setFrom(clases.get(clase).getModelElement());
    r.setTo(clases.get(interfaz).getModelElement());
    unir(d, r, clase, interfaz);
  }

  void relacion(IDiagramUIModel d, String a, String b, String ma, String mb, String rolB, String tipo) {
    IAssociation r = f.createAssociation();
    r.setFrom(clases.get(a).getModelElement());
    r.setTo(clases.get(b).getModelElement());
    IAssociationEnd ea = (IAssociationEnd) r.getFromEnd(), eb = (IAssociationEnd) r.getToEnd();
    ea.setMultiplicity(ma);
    eb.setMultiplicity(mb);
    if (rolB != null) eb.setName(rolB);
    if ("composicion".equals(tipo)) ea.setAggregationKind(IAssociationEnd.AGGREGATION_KIND_COMPOSITED);
    if ("agregacion".equals(tipo)) ea.setAggregationKind(IAssociationEnd.AGGREGATION_KIND_SHARED);
    unir(d, r, a, b);
  }

  void depende(IDiagramUIModel d, String a, String b) {
    IDependency r = f.createDependency();
    r.setFrom(clases.get(a).getModelElement());
    r.setTo(clases.get(b).getModelElement());
    r.addStereotype("use");
    unir(d, r, a, b);
  }

  @Override
  void logica() {
    try {
      IDiagramUIModel d = diagrama(DiagramManager.DIAGRAM_TYPE_CLASS_DIAGRAM, "03 · Vista logica");
      ((IClassDiagramUIModel) d).setShowClassEmptyCompartments(IClassDiagramUIModel.SHOW_CLASS_EMPTY_COMPARTMENTS_NO);
      cargarClases(d);

      // Distribucion por filas: superclase y enumeraciones de la fila 2 arriba, entidades en el centro,
      // enumeraciones de la fila 3 e interfaz abajo.
      List<List<String>> filas = List.of(
          List.of("Rol", "Especie", "Sexo", "RegistroActivable", "CategoriaServicio", "CategoriaProducto"),
          List.of("Usuario", "Cliente", "Mascota", "Servicio", "Producto"),
          List.of("CambioCliente", "Atencion", "Cita", "Pedido", "DetallePedido", "MovimientoInventario"),
          List.of("TipoAtencion", "EstadoCita", "EstadoPedido", "ModalidadEntrega", "TipoMovimiento"),
          List.of("EstadoConTransiciones"));
      int y = 200, anchoMax = 0;
      List<int[]> filasMedidas = new ArrayList<>();
      for (List<String> fila : filas) {
        int ancho = 0, alto = 0;
        for (String c : fila) {
          ancho += clases.get(c).getWidth() + 90;
          alto = Math.max(alto, clases.get(c).getHeight());
        }
        filasMedidas.add(new int[] {ancho - 90, alto});
        anchoMax = Math.max(anchoMax, ancho - 90);
      }
      for (int f2 = 0; f2 < filas.size(); f2++) {
        int x = 60 + (anchoMax - filasMedidas.get(f2)[0]) / 2;
        for (String c : filas.get(f2)) {
          IClassUIModel u = clases.get(c);
          int yy = f2 == 0 ? y + filasMedidas.get(f2)[1] - u.getHeight() : y; // fila superior alineada abajo
          u.setBounds(x, yy, u.getWidth(), u.getHeight());
          x += u.getWidth() + 90;
        }
        y += filasMedidas.get(f2)[1] + 170;
      }

      // Herencia: registros con baja logica
      for (String h : List.of("Usuario", "Cliente", "Mascota", "Servicio", "Producto")) hereda(d, h, "RegistroActivable");
      // Realizacion de interfaz: estados con ciclo de vida
      realiza(d, "EstadoCita", "EstadoConTransiciones");
      realiza(d, "EstadoPedido", "EstadoConTransiciones");
      // Asociaciones, agregacion y composicion
      relacion(d, "Usuario", "Cliente", "0..1", "0..1", "ficha", "asociacion");
      relacion(d, "Cliente", "CambioCliente", "1", "0..*", "historial", "composicion");
      relacion(d, "Cliente", "Mascota", "1", "0..*", "mascotas", "agregacion");
      relacion(d, "Mascota", "Atencion", "1", "0..*", "historial clinico", "asociacion");
      relacion(d, "Mascota", "Cita", "1", "0..*", "citas", "asociacion");
      relacion(d, "Servicio", "Cita", "1", "0..*", null, "asociacion");
      relacion(d, "Usuario", "Cita", "0..1", "0..*", "atiende", "asociacion");
      relacion(d, "Cliente", "Pedido", "1", "0..*", "pedidos", "asociacion");
      relacion(d, "Pedido", "DetallePedido", "1", "1..*", "detalles", "composicion");
      relacion(d, "Producto", "DetallePedido", "1", "0..*", null, "asociacion");
      relacion(d, "Producto", "MovimientoInventario", "1", "0..*", "movimientos", "asociacion");
      // Esta asociacion se dibuja por debajo de la fila para no cruzar DetallePedido.
      IClassUIModel ped = clases.get("Pedido"), mov = clases.get("MovimientoInventario");
      int fondo = Math.max(ped.getY() + ped.getHeight(), mov.getY() + mov.getHeight()) + 45;
      int px = ped.getX() + ped.getWidth() - 40, mx = mov.getX() + 40;
      ruta = new Point[] {new Point(px, ped.getY() + ped.getHeight()), new Point(px, fondo), new Point(mx, fondo), new Point(mx, mov.getY() + mov.getHeight())};
      relacion(d, "Pedido", "MovimientoInventario", "0..1", "0..*", null, "asociacion");
      // Dependencias hacia las enumeraciones usadas como tipos
      depende(d, "Usuario", "Rol");
      depende(d, "Mascota", "Especie");
      depende(d, "Mascota", "Sexo");
      depende(d, "Atencion", "TipoAtencion");
      depende(d, "Servicio", "CategoriaServicio");
      depende(d, "Cita", "EstadoCita");
      depende(d, "Producto", "CategoriaProducto");
      depende(d, "MovimientoInventario", "TipoMovimiento");
      depende(d, "Pedido", "EstadoPedido");
      depende(d, "Pedido", "ModalidadEntrega");

      texto(d, "Vista logica", 60, 20, anchoMax, 80, 34, AMARILLO, true);
      texto(d, "Modelo de dominio de Jovan Pets · Atributos, constructores, getters, setters y operaciones tomados del codigo Java",
          60, 112, anchoMax, 40, 18, Color.WHITE, false);
      texto(d,
          "Relaciones: triangulo hueco = herencia (RegistroActivable: baja logica) · triangulo punteado = realizacion de la interfaz EstadoConTransiciones · rombo lleno = composicion\n"
              + "(Pedido-DetallePedido, Cliente-CambioCliente) · rombo hueco = agregacion (Cliente-Mascota) · linea = asociacion · flecha punteada <<use>> = dependencia hacia enumeraciones.\n"
              + "El diseno fisico en MongoDB (colecciones, referencias e indices) se documenta en docs/ARQUITECTURA.md.",
          60, y - 60, anchoMax, 90, 17, Color.WHITE, false);
      conteos.append("03: ").append(clases.size()).append(" clases (12 clases de dominio, 1 interfaz, 10 enumeraciones), extraidas de backend/src por extraer-modelo.js; ")
          .append("herencia, realizacion, asociacion, agregacion, composicion y dependencia.\n");
      dm.openDiagram(d);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  // ================================================================ 05 · Vista de despliegue

  @Override
  void despliegue() {
    IDiagramUIModel d = diagrama(DiagramManager.DIAGRAM_TYPE_DEPLOYMENT, "05 · Vista de despliegue");
    titulo(d, "Vista de despliegue", 2000, "Jovan Pets publicado · Vercel (frontend), Render (backend) y MongoDB Atlas (base de datos)");

    IShapeUIModel equipo = nodo(d, null, null, "device", ":Equipo del usuario\n(PC, tableta o movil)", 60, 200, 520, 430, new Color(250, 250, 250), 18);
    IShapeUIModel navegador = nodo(d, equipo.getModelElement(), equipo, "executionEnvironment", ":Navegador web", 95, 290, 450, 310, BLANCO, 16);
    artefacto(d, navegador, "Jovan Pets SPA\n(React 18 + TypeScript)", "Aplicacion de una sola pagina descargada desde Vercel.", 130, 380, 380, 110);
    texto(d, "Rutas protegidas por rol\nToken JWT en sessionStorage", 130, 510, 380, 60, 14, BLANCO, false);

    IShapeUIModel vercel = nodo(d, null, null, "device", ":Vercel\n(red de distribucion)", 60, 760, 520, 340, new Color(250, 250, 250), 18);
    IShapeUIModel estaticos = nodo(d, vercel.getModelElement(), vercel, "executionEnvironment", ":Hosting estatico HTTPS\nfrontend-kohl-pi-40.vercel.app", 95, 850, 450, 220, BLANCO, 15);
    artefacto(d, estaticos, "frontend/dist\n(build de Vite)", "npm run build con VITE_API_URL apuntando a Render; vercel.json redirige las rutas a index.html.", 130, 960, 380, 85);

    IShapeUIModel render = nodo(d, null, null, "device", ":Render\n(servicio web)", 740, 200, 640, 900, new Color(250, 250, 250), 18);
    IShapeUIModel docker = nodo(d, render.getModelElement(), render, "executionEnvironment", ":Contenedor Docker\njovan-pets-api.onrender.com", 775, 300, 570, 770, BLANCO, 15);
    IShapeUIModel boot = nodo(d, docker.getModelElement(), docker, "executionEnvironment", ":JVM 21 + Spring Boot 3.5\nTomcat embebido · puerto PORT", 805, 400, 510, 640, AZUL_CAPA, 15);
    artefacto(d, boot, "jovan-pets-backend-1.0.0.jar", "Construido con backend/Dockerfile (Maven + JRE 21).", 840, 500, 440, 70);
    texto(d, "API REST /api/** (controllers + DTO)\nSpring Security + JWT\nServicios y reglas de negocio\nRepositorios Spring Data MongoDB",
        840, 590, 440, 120, 15, AZUL_CAPA, false);
    IShapeUIModel config = artefacto(d, boot, "Variables de entorno de Render\n(MONGODB_URI, JWT_SECRET, CORS_ORIGENES...)",
        "Configuracion externa; los secretos no estan en el repositorio.", 840, 740, 440, 90);
    ((IModelElement) config.getModelElement()).addStereotype("deployment spec");

    IShapeUIModel atlas = nodo(d, null, null, "device", ":MongoDB Atlas\n(AWS us-east-1, cluster M0)", 1540, 200, 440, 900, new Color(250, 250, 250), 18);
    IShapeUIModel replica = nodo(d, atlas.getModelElement(), atlas, "executionEnvironment", ":Replica set de 3 nodos\ncluster0 · MongoDB", 1575, 300, 370, 770, NARANJA_CAPA, 15);
    artefacto(d, replica, "Base de datos jovanpets", "Colecciones e indices creados por Spring Data MongoDB.", 1605, 400, 310, 70);
    texto(d, "Colecciones:\nusuarios · clientes\nmascotas · atenciones\nservicios · citas\nproductos\nmovimientos_inventario\npedidos · contadores",
        1605, 490, 310, 220, 15, NARANJA_CAPA, false);

    via(d, navegador, estaticos, "Descarga de la SPA", "HTTPS", new int[][] {{320, 600}, {320, 850}}, 335, 680, 240, 60);
    via(d, navegador, boot, "Peticiones REST JSON\nAuthorization: Bearer JWT", "HTTPS", new int[][] {{545, 440}, {805, 440}}, 560, 330, 240, 100);
    via(d, boot, replica, "mongodb+srv sobre TLS\n(driver Java)", "TCP 27017", new int[][] {{1315, 640}, {1575, 640}}, 1330, 540, 240, 90);

    texto(d,
        "Recorrido de una solicitud: (1) el usuario abre frontend-kohl-pi-40.vercel.app y el navegador descarga la SPA desde Vercel -> (2) la SPA envia la peticion HTTPS con el token JWT\n"
            + "a jovan-pets-api.onrender.com -> (3) Spring Security valida el token y el controller delega al service -> (4) los repositories consultan o modifican MongoDB Atlas -> (5) la respuesta JSON vuelve a la SPA.\n"
            + "Desarrollo local: los mismos tres nodos en el equipo del desarrollador (Vite :5173, Spring Boot :8080) usando la misma base de Atlas. CORS_ORIGENES solo acepta el dominio de Vercel.",
        60, 1140, 1920, 110, 16, BLANCO, false);
    dm.openDiagram(d);
  }
}
