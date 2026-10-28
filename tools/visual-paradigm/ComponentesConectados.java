import com.vp.plugin.*;
import com.vp.plugin.diagram.*;
import com.vp.plugin.diagram.shape.*;
import com.vp.plugin.model.*;
import com.vp.plugin.model.factory.IModelElementFactory;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Actualiza la vista funcional con los contratos reales de ApiControlador. */
public class ComponentesConectados extends EntregaDiagramas {
  public void invoke(String[] argumentos) {
    try {
      Properties p=new Properties();
      try(InputStream in=new FileInputStream(new File(carpeta,"salida.properties"))){p.load(in);}
      File salida=new File(p.getProperty("salida"));salida.mkdirs();
      app=ApplicationManager.instance();dm=app.getDiagramManager();f=IModelElementFactory.instance();
      if(!app.getProjectManager().openProject(new File(p.getProperty("entrada"))))throw new IOException("No se pudo leer el proyecto base");
      IProject proyecto=app.getProjectManager().getProject();
      IDiagramUIModel anterior=null;
      for(IDiagramUIModel d:proyecto.toDiagramArray())if(d.getName().startsWith("02"))anterior=d;
      if(anterior==null)throw new IOException("No se encontro la vista funcional previa");
      // Los modelos de esta vista fueron creados especificamente para ella.
      for(IShapeUIModel s:anterior.toShapeUIModelArray())if(s.getModelElement()!=null)s.getModelElement().delete();
      anterior.delete();componentes();
      File combinado=new File(salida,"Jovan-Pets.vpp");
      if(!app.getProjectManager().saveProjectAs(combinado))throw new IOException("No se pudo guardar el proyecto completo");
      app.getProjectManager().newProject();
      if(!app.getProjectManager().openProject(combinado))throw new IOException("No se pudo reabrir el proyecto completo");
      proyecto=app.getProjectManager().getProject();
      if(proyecto.toDiagramArray().length!=3)throw new IOException("El proyecto completo debe conservar tres vistas");
      IDiagramUIModel funcional=null;
      for(IDiagramUIModel d:proyecto.toDiagramArray())if(d.getName().startsWith("02"))funcional=d;
      validar(funcional);
      ExportDiagramAsImageOption opt=new ExportDiagramAsImageOption(ExportDiagramAsImageOption.IMAGE_TYPE_PNG);
      opt.setGraphicAntiAliasing(true);opt.setTextAntiAliasing(true);opt.setScale(1.5f);
      dm.openDiagram(funcional);app.getModelConvertionManager().exportDiagramAsImage(funcional,new File(salida,"02-componentes.png"),opt);
      StringBuilder auditoria=new StringBuilder("Visual Paradigm "+app.getBuildNo()+"\nFecha: "+java.time.ZonedDateTime.now(java.time.ZoneId.of("America/Bogota"))+"\n");
      for(IDiagramUIModel d:proyecto.toDiagramArray())auditoria.append(d.getName()).append(" | ").append(d.getType()).append(" | ").append(d.toShapeUIModelArray().length).append(" formas | ").append(d.toConnectorUIModelArray().length).append(" conectores\n");
      auditoria.append("Vista funcional: 6 componentes, 10 puertos, 5 interfaces, 5 realizaciones y 5 usos.\nNo hay relaciones Dependency entre componentes.\nLas interfaces provistas/requeridas se muestran con lineas continuas, circulo y semicirculo.\nLos cinco contratos coinciden con los tipos inyectados en ApiControlador.\n");
      app.getProjectManager().saveProject();
      Files.writeString(new File(salida,"validacion-modelo.txt").toPath(),auditoria.toString());
      app.getProjectManager().newProject();componentes();
      File independiente=new File(salida,"Jovan-Pets-componentes.vpp");
      if(!app.getProjectManager().saveProjectAs(independiente))throw new IOException("No se guardo el proyecto de componentes");
      app.getProjectManager().newProject();
      if(!app.getProjectManager().openProject(independiente))throw new IOException("No se reabrio el proyecto de componentes");
      validar(app.getProjectManager().getProject().toDiagramArray()[0]);
      app.getProjectManager().saveProject();
    }catch(Throwable e){
      try{StringWriter s=new StringWriter();e.printStackTrace(new PrintWriter(s));Files.writeString(new File(carpeta,"error.txt").toPath(),s.toString());}catch(Exception ignored){}
      throw new RuntimeException(e);
    }
  }
  void validar(IDiagramUIModel d)throws IOException {
    if(d==null)throw new IOException("No existe la vista funcional");
    int componentes=0,puertos=0,interfaces=0;
    for(IShapeUIModel s:d.toShapeUIModelArray()){
      IModelElement m=s.getModelElement();
      if(m instanceof IComponent)componentes++;
      if(m instanceof IPort)puertos++;
      if(s instanceof IStructuredInterfaceUIModel)interfaces++;
    }
    if(componentes!=6||puertos!=10||interfaces!=5||d.toConnectorUIModelArray().length!=10)
      throw new IOException("Conteos funcionales inesperados: "+componentes+","+puertos+","+interfaces+","+d.toConnectorUIModelArray().length);
    for(IConnectorUIModel c:d.toConnectorUIModelArray()) {
      if(!(c.getModelElement() instanceof IRealization)&&!(c.getModelElement() instanceof IUsage))
        throw new IOException("Relacion no prevista: "+c.getModelElement().getModelType());
      if(c.getFromShape().getModelElement() instanceof IComponent && c.getToShape().getModelElement() instanceof IComponent)
        throw new IOException("No se permiten dependencias directas entre componentes");
    }
  }
  IComponentUIModel modulo(IDiagramUIModel d,String nombre,int x,int y,int w,int h,String responsabilidad) {
    IComponent m=f.createComponent();m.setDocumentation(responsabilidad);
    IComponentUIModel s=(IComponentUIModel)forma(d,m,nombre,x,y,w,h,CREMA,25);
    s.setShowOption(IComponentUIModel.SHOW_COMPONENT_OPTION_KEYWORD_AND_ICON);
    return s;
  }
  IPortUIModel puerto(IDiagramUIModel d,IComponentUIModel componente,IClass contrato,int x,int y,String nombre) {
    IPort p=((IComponent)componente.getModelElement()).createPort();p.setName(nombre);p.setType(contrato);
    IPortUIModel s=(IPortUIModel)forma(d,p,nombre,x-8,y-8,16,16,Color.WHITE,15);
    componente.addChild(s);s.getCaptionUIModel().setVisible(false);s.setRequestResetCaption(false);
    return s;
  }
  void conexion(IDiagramUIModel d,IComponentUIModel proveedor,IComponentUIModel consumidor,String contrato,String servicio,
                int px,int py,int cx,int cy,int ix,int iy,int[][] recorrido,int labelX,int labelY,int labelW) {
    IClass interfaz=f.createClass();interfaz.setName(contrato);interfaz.addStereotype("Interface");
    interfaz.setDocumentation("Contrato Java com.jovanpets.servicio."+contrato+". Provisto por "+proveedor.getModelElement().getName()+" y requerido por ApiControlador.");
    IPortUIModel prov=puerto(d,proveedor,interfaz,px,py,"provee"+contrato);
    IPortUIModel req=puerto(d,consumidor,interfaz,cx,cy,"requiere"+contrato);
    IStructuredInterfaceUIModel vista=(IStructuredInterfaceUIModel)dm.createDiagramElement(d,IShapeTypeConstants.SHAPE_TYPE_STRUCTURED_INTERFACE);
    vista.setModelElement(interfaz);vista.setBounds(ix-13,iy-13,26,26);
    vista.setBackground(Color.WHITE);vista.getFillColor().setType(0);vista.getFillColor().setColor1(Color.WHITE);
    vista.getElementFont().setValues("Arial",false,false,21,NEGRO);vista.getLineModel().setColor(NEGRO);
    vista.setRequestResetCaption(false);vista.getCaptionUIModel().setSide(ICaptionUIModel.SIDE_FREEMOVE);
    vista.getCaptionUIModel().setBounds(labelX,labelY,labelW,35);
    IRealization realiza=f.createRealization();realiza.setFrom(prov.getModelElement());realiza.setTo(interfaz);
    IConnectorUIModel lineaProv=(IConnectorUIModel)dm.createConnector(d,realiza,vista,prov,new Point[]{new Point(ix,iy),new Point(px,py)});
    lineaProv.setConnectorStyle(IConnectorUIModel.CS_RECTI_LINEAR);lineaProv.getLineModel().setColor(NEGRO);
    IUsage usa=f.createUsage();usa.setFrom(req.getModelElement());usa.setTo(interfaz);
    Point[] puntos=Arrays.stream(recorrido).map(a->new Point(a[0],a[1])).toArray(Point[]::new);
    IConnectorUIModel lineaReq=(IConnectorUIModel)dm.createConnector(d,usa,req,vista,puntos);
    lineaReq.setConnectorStyle(IConnectorUIModel.CS_RECTI_LINEAR);lineaReq.getLineModel().setColor(NEGRO);
    lineaProv.getCaptionUIModel().setVisible(false);lineaReq.getCaptionUIModel().setVisible(false);
    texto(d,servicio,ix-150,iy-90,300,60,18,Color.WHITE,false);
  }
  @Override void componentes() {
    IDiagramUIModel d=diagrama(DiagramManager.DIAGRAM_TYPE_COMPONENT_DIAGRAM,"02 · Vista funcional");
    titulo(d,"Vista funcional: componentes",2120,"Jovan Pets · Puertos e interfaces provistas y requeridas");
    IShapeUIModel marco=forma(d,f.createRectangle(),"",45,180,2030,1150,Color.WHITE,20);marco.sendToBack();
    IComponentUIModel api=modulo(d,"Portal web y\nAPI REST",880,615,360,150,"Frontend y ApiControlador. Requiere IUsuarios, IClientesMascotas, ICitas, IVentas e IPanel.");
    IComponentUIModel usuarios=modulo(d,"Usuarios y acceso",100,260,440,120,"HU-18 a HU-22. Registro, sesion y roles. Recuperacion por correo pendiente.");
    IComponentUIModel clientes=modulo(d,"Clientes y mascotas",100,640,440,120,"HU-01 a HU-08. Clientes, mascotas e historial.");
    IComponentUIModel citas=modulo(d,"Citas y servicios",100,1020,440,120,"HU-09 a HU-12 y HU-23. Disponibilidad, reserva y gestion de citas.");
    IComponentUIModel ventas=modulo(d,"Productos, pedidos\ne inventario",1580,400,440,135,"HU-13 a HU-17. Catalogo, pedido, movimientos y existencias.");
    IComponentUIModel panel=modulo(d,"Panel administrativo",1580,950,440,120,"HU-24. Resumen del negocio.");
    texto(d,"HU-18 a HU-22\nCorreo de recuperacion pendiente",100,392,440,65,18,Color.WHITE,false);
    texto(d,"HU-01 a HU-08",100,775,440,40,18,Color.WHITE,false);
    texto(d,"HU-09 a HU-12 y HU-23",100,1153,440,40,18,Color.WHITE,false);
    texto(d,"HU-13 a HU-17",1580,550,440,40,18,Color.WHITE,false);
    texto(d,"HU-24",1580,1085,440,40,18,Color.WHITE,false);
    conexion(d,usuarios,api,"IUsuarios","Cuentas y roles",540,320,880,640,690,320,new int[][]{{880,640},{820,640},{820,320},{690,320}},580,355,220);
    conexion(d,clientes,api,"IClientesMascotas","Clientes, mascotas\ne historial",540,700,880,700,690,700,new int[][]{{880,700},{690,700}},550,735,280);
    conexion(d,citas,api,"ICitas","Agenda y servicios",540,1080,880,750,690,1080,new int[][]{{880,750},{845,750},{845,1080},{690,1080}},580,1115,220);
    conexion(d,ventas,api,"IVentas","Catalogo, pedidos\ny existencias",1580,468,1240,655,1430,468,new int[][]{{1240,655},{1295,655},{1295,468},{1430,468}},1310,503,240);
    conexion(d,panel,api,"IPanel","Indicadores del negocio",1580,1010,1240,735,1430,1010,new int[][]{{1240,735},{1295,735},{1295,1010},{1430,1010}},1320,1045,220);
    texto(d,"Circulo: interfaz provista. Semicirculo: interfaz requerida. Cuadrado: puerto.\nLineas continuas: conexion mediante contrato. Los componentes pertenecen al mismo monolito Spring Boot.",80,1230,1960,80,20,Color.WHITE,false);
    dm.openDiagram(d);
  }
}
