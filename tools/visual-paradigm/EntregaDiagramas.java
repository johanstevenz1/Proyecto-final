import com.vp.plugin.*;
import com.vp.plugin.diagram.*;
import com.vp.plugin.diagram.shape.IClassUIModel;
import com.vp.plugin.diagram.connector.IAssociationUIModel;
import com.vp.plugin.model.*;
import com.vp.plugin.model.factory.IModelElementFactory;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Crea modelos nativos y exporta las tres vistas con el motor de Visual Paradigm. */
public class EntregaDiagramas implements VPPlugin, VPPluginCommandLineSupport {
  static final Color NEGRO=new Color(35,35,35), AMARILLO=new Color(239,207,125);
  static final Color CREMA=new Color(255,244,207), VERDE=new Color(157,248,157);
  ApplicationManager app; DiagramManager dm; IModelElementFactory f;
  File carpeta; final java.util.List<IDiagramUIModel> diagramas=new ArrayList<>();
  final Map<String,IShapeUIModel> clases=new LinkedHashMap<>();
  public void loaded(VPPluginInfo info) { carpeta=info.getPluginDir(); }
  public void unloaded() {}
  public static void ejecutar(File dir) { EntregaDiagramas e=new EntregaDiagramas(); e.carpeta=dir; e.invoke(new String[0]); }
  public void invoke(String[] argumentos) {
    try {
      Properties p=new Properties();
      try(InputStream in=new FileInputStream(new File(carpeta,"salida.properties"))){p.load(in);}
      File salida=new File(p.getProperty("salida")); salida.mkdirs();
      app=ApplicationManager.instance();dm=app.getDiagramManager();f=IModelElementFactory.instance();
      if(!app.getProjectManager().newProject())throw new IOException("No se pudo crear el proyecto");
      contexto();componentes();logica();
      File proyecto=new File(salida,"Jovan-Pets.vpp");
      if(!app.getProjectManager().saveProjectAs(proyecto))throw new IOException("No se guardo el VPP");
      if(!app.getProjectManager().newProject()||!app.getProjectManager().openProject(proyecto))
        throw new IOException("No se pudo reabrir el VPP guardado");
      IProject modelo=app.getProjectManager().getProject();
      if(modelo.toDiagramArray().length!=3)throw new IOException("Se esperaban tres diagramas");
      if(modelo.toAllLevelModelElementArray(IModelElementFactory.MODEL_TYPE_DEPENDENCY).length!=0)
        throw new IOException("Hay relaciones Dependency no permitidas");
      StringBuilder auditoria=new StringBuilder("Generado y reabierto por Visual Paradigm "+app.getBuildNo()+"\n");
      for(IDiagramUIModel d:modelo.toDiagramArray()) {
        dm.openDiagram(d);
        String nombre=d.getName().startsWith("01")?"01-contexto":d.getName().startsWith("02")?"02-componentes":"03-clases";
        ExportDiagramAsImageOption opt=new ExportDiagramAsImageOption(ExportDiagramAsImageOption.IMAGE_TYPE_PNG);
        opt.setGraphicAntiAliasing(true);opt.setTextAntiAliasing(true);opt.setScale(1.5f);
        app.getModelConvertionManager().exportDiagramAsImage(d,new File(salida,nombre+".png"),opt);
        auditoria.append(d.getName()).append(" | tipo ").append(d.getType()).append(" | ")
          .append(d.toShapeUIModelArray().length).append(" formas | ")
          .append(d.toConnectorUIModelArray().length).append(" conectores\n");
        if(d.getName().startsWith("02") && d.toConnectorUIModelArray().length!=0)
          throw new IOException("La vista funcional debe estar libre de relaciones entre componentes");
      }
      auditoria.append("Dependency: 0\nOrigen: Open API nativa; exportacion: ModelConvertionManager.exportDiagramAsImage\n")
        .append("Clases de dominio: 10. Atributos reflejan las entidades Java; operaciones de negocio en servicios.\n")
        .append("Fecha: ").append(java.time.ZonedDateTime.now(java.time.ZoneId.of("America/Bogota"))).append("\n");
      app.getProjectManager().saveProject();
      Files.writeString(new File(salida,"validacion-modelo.txt").toPath(),auditoria.toString());
    } catch(Throwable e) {
      try {StringWriter s=new StringWriter();e.printStackTrace(new PrintWriter(s));
        Files.writeString(new File(carpeta,"error.txt").toPath(),s.toString());}catch(Exception ign){}
      throw new RuntimeException(e);
    }
  }
  IDiagramUIModel diagrama(String tipo,String nombre) {
    IDiagramUIModel d=dm.createDiagram(tipo);d.setName(nombre);
    d.setGridVisible(false);d.setAlignToGrid(false);d.setAutoFitShapesSize(false);
    d.setDiagramBackground(Color.WHITE);diagramas.add(d);return d;
  }
  IShapeUIModel forma(IDiagramUIModel d,IModelElement m,String nombre,int x,int y,int w,int h,Color color,int tam) {
    m.setName(nombre);IShapeUIModel s=(IShapeUIModel)dm.createDiagramElement(d,m);
    s.setBounds(x,y,w,h);s.setBackground(color);s.setForeground(NEGRO);
    s.getLineModel().setColor(NEGRO);
    s.getFillColor().setType(0);s.getFillColor().setColor1(color);
    s.getElementFont().setValues("Arial",false,false,tam,NEGRO);s.setRequestResetCaption(true);return s;
  }
  void texto(IDiagramUIModel d,String t,int x,int y,int w,int h,int tam,Color color,boolean negrita) {
    IShapeUIModel s=forma(d,f.createRectangle(),t,x,y,w,h,color,tam);
    s.setForeground(color);s.getElementFont().setColor(NEGRO);s.getElementFont().setBold(negrita);
    s.getLineModel().setTransparency(100);
  }
  void titulo(IDiagramUIModel d,String t,int ancho,String subtitulo) {
    texto(d,t,25,20,ancho-50,85,32,AMARILLO,true);
    texto(d,subtitulo,35,112,ancho-70,45,17,Color.WHITE,false);
  }
  void flujo(IDiagramUIModel d,IShapeUIModel a,IShapeUIModel b,String nombre,int[][] puntos,int x,int y,int w,int h) {
    IYdDfdDataFlow r=f.createYdDfdDataFlow();r.setFrom(a.getModelElement());r.setTo(b.getModelElement());r.setName(nombre);
    Point[] ps=Arrays.stream(puntos).map(p->new Point(p[0],p[1])).toArray(Point[]::new);
    IConnectorUIModel c=(IConnectorUIModel)dm.createConnector(d,r,a,b,ps);
    c.setForeground(NEGRO);c.getElementFont().setValues("Arial",false,false,17,NEGRO);
    c.setConnectorStyle(IConnectorUIModel.CS_RECTI_LINEAR);
    c.setRequestResetCaption(false);c.getCaptionUIModel().setSide(ICaptionUIModel.SIDE_FREEMOVE);
    c.getCaptionUIModel().setBounds(x,y,w,h);
  }
  void contexto() {
    IDiagramUIModel d=diagrama("YourdonDeMarcoDFD","01 · Vista de contexto");
    titulo(d,"Vista de contexto",1600,"Jovan Pets · Un único sistema y cuatro entidades externas · HU-01 a HU-24");
    IYdDfdProcess proceso=f.createYdDfdProcess();proceso.setDfId("0");
    IShapeUIModel sistema=forma(d,proceso,"Jovan Pets",675,400,250,250,Color.WHITE,25);
    IShapeUIModel cliente=forma(d,f.createYdDfdExternalEntity(),"Cliente",660,190,280,70,Color.WHITE,22);
    IShapeUIModel admin=forma(d,f.createYdDfdExternalEntity(),"Administrador",70,470,280,100,Color.WHITE,22);
    IShapeUIModel personal=forma(d,f.createYdDfdExternalEntity(),"Personal veterinario",1250,470,280,100,Color.WHITE,22);
    IShapeUIModel correo=forma(d,f.createYdDfdExternalEntity(),"Servicio de correo\n(integracion pendiente)",625,820,350,90,new Color(245,245,245),20);
    flujo(d,cliente,sistema,"Datos de cuenta, mascotas,\nsolicitudes y pedidos",new int[][]{{720,260},{720,400}},460,295,245,75);
    flujo(d,sistema,cliente,"Catalogo, horarios, historial\ny estados de sus solicitudes",new int[][]{{870,400},{870,260}},900,295,280,75);
    flujo(d,admin,sistema,"Datos de clientes, mascotas,\nservicios, productos y roles",new int[][]{{350,495},{675,495}},360,420,300,65);
    flujo(d,sistema,admin,"Listados, confirmaciones\ny metricas del negocio",new int[][]{{675,550},{350,550}},370,560,290,65);
    flujo(d,personal,sistema,"Gestion de citas,\natenciones y existencias",new int[][]{{1250,550},{925,550}},940,560,290,65);
    flujo(d,sistema,personal,"Agenda, historial clinico\ny alertas de stock",new int[][]{{925,495},{1250,495}},940,420,290,65);
    flujo(d,sistema,correo,"Solicitud de envio del enlace\nde recuperacion [HU-22]",new int[][]{{720,650},{720,820}},435,705,270,65);
    flujo(d,correo,sistema,"Resultado del envio\n(previsto; no implementado)",new int[][]{{870,820},{870,650}},900,705,290,65);
    texto(d,"Limite: gestion veterinaria y comercial. Sin pagos electronicos ni sistemas externos de reparto.\nCorreo: requisito confirmado, integracion pendiente. Las flechas representan datos; no modulos internos.",75,960,1450,80,18,Color.WHITE,false);
    dm.openDiagram(d);
  }
  void componente(IDiagramUIModel d,String nombre,String contrato,String responsabilidad,int x,int y,int numero) {
    IComponent m=f.createComponent();m.setDocumentation(responsabilidad+"\nContrato Java: "+contrato);
    forma(d,m,nombre,x,y,510,105,CREMA,24);
    texto(d,responsabilidad+"\nContrato: "+contrato,x,y+116,510,95,17,Color.WHITE,false);
    IShapeUIModel n=forma(d,f.createOval(),String.valueOf(numero),x-50,y+30,36,36,Color.WHITE,20);
    n.setForeground(new Color(210,65,65));n.getElementFont().setColor(new Color(210,65,65));
    n.getLineModel().setColor(new Color(210,65,65));
  }
  void componentes() {
    IDiagramUIModel d=diagrama(DiagramManager.DIAGRAM_TYPE_COMPONENT_DIAGRAM,"02 · Vista funcional");
    titulo(d,"Vista funcional",1500,"Componentes funcionales del monolito Spring Boot · Sin relaciones de dependencia");
    IShapeUIModel marco=forma(d,f.createRectangle(),"",65,180,1370,930,Color.WHITE,18);marco.sendToBack();
    texto(d,"Jovan Pets · responsabilidades y contratos",180,190,1140,40,21,Color.WHITE,true);
    componente(d,"Usuarios y acceso","IUsuarios","HU-18 a HU-22 · Cuentas, sesiones y roles.\nHU-22: recuperacion por correo pendiente.",200,270,1);
    componente(d,"Clientes y mascotas","IClientesMascotas","HU-01 a HU-08 · Propietarios, fichas,\nantecedentes, controles y vacunas.",870,270,2);
    componente(d,"Citas y servicios","ICitas","HU-09 a HU-12 y HU-23 · Disponibilidad,\nsolicitudes, calendario y catalogo de servicios.",200,540,3);
    componente(d,"Productos, pedidos e inventario","IVentas","HU-13 a HU-17 · Catalogo, carrito, pedidos,\nmovimientos y alertas de existencias.",870,540,4);
    componente(d,"Panel administrativo","IPanel","HU-24 · Resumen de clientes y mascotas,\ncitas del dia, pedidos y alertas de stock.",530,810,5);
    texto(d,"Cada caja es un componente UML nativo. Los contratos se nombran como en el codigo Java.\nLa comunicacion se describe en la documentacion; esta vista no introduce flechas ni dependencias.",100,1140,1300,80,18,Color.WHITE,false);
    dm.openDiagram(d);
  }
  void clase(IDiagramUIModel d,String nombre,int x,int y,int w,int h,String campos) {
    IClass c=f.createClass();c.setName(nombre);
    c.setDocumentation("Entidad Java: com.jovanpets.dominio."+nombre+". Las operaciones de negocio estan en la capa servicio.");
    for(String dato:campos.split(";")) {
      String[] v=dato.split(":",2);IAttribute a=c.createAttribute();a.setName(v[0]);a.setType(v[1]);a.setVisibility(IAttribute.VISIBILITY_PUBLIC);
    }
    IClassUIModel s=(IClassUIModel)forma(d,c,nombre,x,y,w,h,VERDE,19);
    s.setShowAttributeType(IClassUIModel.ATTR_SHOW_TYPE_ALL);s.setVisibilityStyle(IClassUIModel.VS_UML);
    s.setShowEmptyCompartments(IClassDiagramUIModel.SHOW_CLASS_EMPTY_COMPARTMENTS_NO);clases.put(nombre,s);
  }
  void asociacion(IDiagramUIModel d,String desde,String hasta,String ma,String mb,boolean composicion,int[][] puntos,int ax,int ay,int bx,int by) {
    IShapeUIModel a=clases.get(desde),b=clases.get(hasta);IAssociation r=f.createAssociation();
    r.setFrom(a.getModelElement());r.setTo(b.getModelElement());
    ((IAssociationEnd)r.getFromEnd()).setMultiplicity(ma);((IAssociationEnd)r.getToEnd()).setMultiplicity(mb);
    if(composicion)((IAssociationEnd)r.getFromEnd()).setAggregationKind(IAssociationEnd.AGGREGATION_KIND_COMPOSITED);
    Point[] ps=Arrays.stream(puntos).map(p->new Point(p[0],p[1])).toArray(Point[]::new);
    IAssociationUIModel c=(IAssociationUIModel)dm.createConnector(d,r,a,b,ps);
    c.setConnectorStyle(IConnectorUIModel.CS_RECTI_LINEAR);c.setConnectorLineJumps(IConnectorUIModel.CLJ_ARC);
    c.setShowFromMultiplicity(true);c.setShowToMultiplicity(true);c.setShowFromRoleName(false);c.setShowToRoleName(false);
    c.setShowNavigationArrows(0);c.setSuppressImplied1MultiplicityForAssociationEnd(IAssociationUIModel.SUPPRESS_IMPLIED_1_MULTIPLICITY_FOR_ASSOCIATION_END_NO);
    c.getElementFont().setValues("Arial",false,false,19,NEGRO);
    c.setHasMultiplicityAShape(true);c.setHasMultiplicityBShape(true);
    c.setMultiplicityARectangle(ax,ay,60,30);c.setMultiplicityBRectangle(bx,by,60,30);
    c.getCaptionUIModel().setVisible(false);
  }
  void logica() {
    IDiagramUIModel d=diagrama(DiagramManager.DIAGRAM_TYPE_CLASS_DIAGRAM,"03 · Vista logica");
    ((IClassDiagramUIModel)d).setShowClassEmptyCompartments(IClassDiagramUIModel.SHOW_CLASS_EMPTY_COMPARTMENTS_NO);
    titulo(d,"Vista lógica",1900,"Modelo de dominio · Diez entidades alineadas con Java/JPA · Atributos y multiplicidades");
    clase(d,"Usuario",80,220,300,185,"id:Long;email:String;claveHash:String;rol:Rol;activo:boolean");
    clase(d,"Cliente",530,220,300,235,"id:Long;nombre:String;cedula:String;telefono:String;email:String;direccion:String;activo:boolean");
    clase(d,"Mascota",1020,220,330,270,"id:Long;nombre:String;especie:String;raza:String;sexo:String;fechaNacimiento:LocalDate;peso:BigDecimal;activo:boolean");
    clase(d,"Atencion",1500,220,330,235,"id:Long;tipo:TipoAtencion;fecha:LocalDate;descripcion:String;profesional:String;proximaFecha:LocalDate");
    clase(d,"Pedido",530,700,300,235,"id:Long;fecha:LocalDateTime;estado:EstadoPedido;modalidad:ModalidadEntrega;direccionEntrega:String;total:BigDecimal");
    clase(d,"Servicio",1020,700,330,230,"id:Long;nombre:String;descripcion:String;categoria:String;precio:BigDecimal;duracionMinutos:int;activo:boolean");
    clase(d,"Cita",1500,700,330,205,"id:Long;inicio:LocalDateTime;fin:LocalDateTime;estado:EstadoCita;motivo:String");
    clase(d,"DetallePedido",530,1150,300,205,"id:Long;nombreProducto:String;cantidad:int;precioUnitario:BigDecimal;subtotal:BigDecimal");
    clase(d,"Producto",1020,1150,330,270,"id:Long;nombre:String;descripcion:String;categoria:String;precio:BigDecimal;stock:int;stockMinimo:int;activo:boolean");
    clase(d,"MovimientoInventario",1500,1150,330,235,"id:Long;fecha:LocalDateTime;tipo:TipoMovimiento;cantidad:int;motivo:String;saldo:int");
    asociacion(d,"Usuario","Cliente","0..1","0..1",false,new int[][]{{380,310},{530,310}},390,273,465,273);
    asociacion(d,"Cliente","Mascota","1","0..*",false,new int[][]{{830,335},{1020,335}},845,295,955,295);
    asociacion(d,"Mascota","Atencion","1","0..*",false,new int[][]{{1350,335},{1500,335}},1360,295,1430,295);
    asociacion(d,"Cliente","Pedido","1","0..*",false,new int[][]{{680,455},{680,700}},695,467,695,655);
    asociacion(d,"Mascota","Cita","1","0..*",false,new int[][]{{1200,490},{1200,570},{1665,570},{1665,700}},1215,500,1680,655);
    asociacion(d,"Servicio","Cita","1","0..*",false,new int[][]{{1350,805},{1500,805}},1360,765,1430,765);
    asociacion(d,"Pedido","DetallePedido","1","1..*",true,new int[][]{{680,935},{680,1150}},695,953,695,1107);
    asociacion(d,"Producto","DetallePedido","1","0..*",false,new int[][]{{1020,1250},{830,1250}},955,1210,845,1210);
    asociacion(d,"Producto","MovimientoInventario","1","0..*",false,new int[][]{{1350,1270},{1500,1270}},1360,1230,1430,1230);
    texto(d,"Leyenda: linea continua = asociacion · rombo lleno = composicion Pedido/DetallePedido.\nNo hay herencia: los roles son valores de Usuario. Categoria y stock son atributos de Producto.\nLas operaciones de negocio residen en servicios; no se agregan metodos inexistentes a las entidades.",65,1470,1770,110,18,Color.WHITE,false);
    dm.openDiagram(d);
  }
}
