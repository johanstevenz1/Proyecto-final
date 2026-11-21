import java.util.*;
import java.util.prefs.*;
/** Preferencias efimeras para exportacion CLI, sin escribir en el registro de Windows. */
public final class PreferenciasMemoria implements PreferencesFactory {
  private final Preferences raiz=new Nodo(null,"");
  public Preferences userRoot(){return raiz;}
  public Preferences systemRoot(){return raiz;}
  private static final class Nodo extends AbstractPreferences {
    final Map<String,String> valores=new HashMap<>();
    final Map<String,Nodo> hijos=new HashMap<>();
    Nodo(AbstractPreferences p,String n){super(p,n);}
    protected void putSpi(String k,String v){valores.put(k,v);}
    protected String getSpi(String k){return valores.get(k);}
    protected void removeSpi(String k){valores.remove(k);}
    protected void removeNodeSpi(){valores.clear();hijos.clear();}
    protected String[] keysSpi(){return valores.keySet().toArray(new String[0]);}
    protected String[] childrenNamesSpi(){return hijos.keySet().toArray(new String[0]);}
    protected AbstractPreferences childSpi(String n){return hijos.computeIfAbsent(n,k->new Nodo(this,k));}
    protected void syncSpi(){}
    protected void flushSpi(){}
  }
}
