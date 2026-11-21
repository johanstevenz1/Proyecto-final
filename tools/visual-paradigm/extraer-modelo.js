// Extrae del codigo Java del backend las clases del dominio (atributos, constructores y metodos)
// para que la vista logica de Visual Paradigm coincida exactamente con la implementacion.
// Uso: node extraer-modelo.js <carpeta backend/src/main/java/com/jovanpets> <salida.txt>
const fs = require('fs');
const path = require('path');

const raiz = process.argv[2];
const salida = process.argv[3];

const CLASES = [
  ['document', 'RegistroActivable'], ['document', 'Usuario'], ['document', 'Cliente'], ['document', 'CambioCliente'],
  ['document', 'Mascota'], ['document', 'Atencion'], ['document', 'Servicio'], ['document', 'Cita'],
  ['document', 'Producto'], ['document', 'MovimientoInventario'], ['document', 'Pedido'], ['document', 'DetallePedido'],
  ['domain', 'EstadoConTransiciones'], ['domain', 'Rol'], ['domain', 'Especie'], ['domain', 'Sexo'],
  ['domain', 'TipoAtencion'], ['domain', 'CategoriaServicio'], ['domain', 'EstadoCita'], ['domain', 'CategoriaProducto'],
  ['domain', 'TipoMovimiento'], ['domain', 'EstadoPedido'], ['domain', 'ModalidadEntrega'],
];

const simbolo = (v) => (v === 'private' ? '-' : v === 'protected' ? '#' : '+');

function limpiar(texto) {
  return texto
    .replace(/\/\*[\s\S]*?\*\//g, '')
    .replace(/\/\/.*$/gm, '')
    .replace(/@\w+(\([^)]*\))?/g, '');
}

function parametros(lista) {
  if (!lista.trim()) return '';
  const partes = [];
  let nivel = 0;
  let actual = '';
  for (const ch of lista) {
    if (ch === '<') nivel++;
    if (ch === '>') nivel--;
    if (ch === ',' && nivel === 0) {
      partes.push(actual);
      actual = '';
    } else actual += ch;
  }
  partes.push(actual);
  return partes
    .map((p) => p.trim().replace(/\s+/g, ' '))
    .map((p) => {
      const i = p.lastIndexOf(' ');
      return `${p.slice(i + 1)} : ${p.slice(0, i)}`;
    })
    .join(', ');
}

const lineas = [];
for (const [paquete, nombre] of CLASES) {
  const original = fs.readFileSync(path.join(raiz, paquete, nombre + '.java'), 'utf8');
  const codigo = limpiar(original);
  const esEnum = new RegExp(`enum\\s+${nombre}\\b`).test(codigo);
  const esInterfaz = new RegExp(`interface\\s+${nombre}\\b`).test(codigo);
  const esAbstracta = new RegExp(`abstract\\s+class\\s+${nombre}\\b`).test(codigo);
  const estereotipo = esEnum ? 'enumeration' : esInterfaz ? 'interface' : paquete === 'document' && /@Document/.test(original) ? 'Document' : '';
  const generico = esInterfaz ? (codigo.match(new RegExp(`interface\\s+${nombre}\\s*(<[^{]*>)`)) || [])[1] || '' : '';
  lineas.push(`CLASE|${nombre}${generico.replace(/\s+/g, ' ')}|${estereotipo}|${esAbstracta ? 'abstracta' : ''}|com.jovanpets.${paquete}`);

  if (esEnum) {
    const cuerpo = codigo.slice(codigo.indexOf('{') + 1);
    const fin = Math.min(...[cuerpo.indexOf(';'), cuerpo.indexOf('}')].filter((i) => i >= 0));
    const literales = cuerpo.slice(0, fin).split(',').map((s) => s.trim()).filter(Boolean);
    literales.forEach((l) => lineas.push(`LITERAL|${l}`));
    // Metodos implicitos que Java genera para todo enum
    lineas.push(`+values() : ${nombre}[]`);
    lineas.push(`+valueOf(nombre : String) : ${nombre}`);
  }

  // Atributos
  const reAtributo = /^\s*(private|protected)\s+(?:final\s+)?([\w<>,. ?]+?)\s+(\w+)\s*(=[^;]*)?;/gm;
  let m;
  while ((m = reAtributo.exec(codigo))) lineas.push(`${simbolo(m[1])}${m[3]} : ${m[2].replace(/\s+/g, ' ')}`);

  // Constructores (si no hay ninguno explicito, Java crea el constructor publico por defecto)
  const reConstructor = new RegExp(`^\\s*(public|protected|private)?\\s*${nombre}\\s*\\(([^)]*)\\)\\s*\\{`, 'gm');
  let constructores = 0;
  if (!esEnum && !esInterfaz) {
    while ((m = reConstructor.exec(codigo))) {
      constructores++;
      lineas.push(`${simbolo(m[1] || 'public')}${nombre}(${parametros(m[2])})`);
    }
    if (constructores === 0) lineas.push(`+${nombre}()`);
  }

  // Metodos
  const reMetodo = /^\s*(public|protected|default)?\s*(static\s+)?(?:abstract\s+)?([\w<>,.? ]+?)\s+(\w+)\s*\(([^)]*)\)\s*[{;]/gm;
  while ((m = reMetodo.exec(codigo))) {
    const [, vis, , tipo, metodo, args] = m;
    if (['return', 'new', 'else', 'if', 'switch', 'throw'].includes(tipo.trim())) continue;
    if (metodo === nombre) continue;
    if (!vis && !esInterfaz) continue;
    lineas.push(`${simbolo(vis === 'default' || (esInterfaz && !vis) ? 'public' : vis)}${metodo}(${parametros(args)}) : ${tipo.trim().replace(/\s+/g, ' ')}`);
  }
}
fs.writeFileSync(salida, lineas.join('\n') + '\n', 'utf8');
console.log(`${CLASES.length} clases, ${lineas.length} lineas -> ${salida}`);
