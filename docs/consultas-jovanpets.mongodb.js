/* global use, db */
// Consultas de la base de datos de Jovan Pets (MongoDB Atlas).
// Abrir en VS Code con la extension "MongoDB for VS Code" conectada al cluster y ejecutar
// todo con el boton ▶ (Run All) o seleccionar una consulta y usar "Run Selected Lines".
// Todas las consultas son de LECTURA: no modifican datos.

use('jovanpets');

// 1. Cantidad de documentos por coleccion
db.getCollectionNames().sort().map((c) => ({ coleccion: c, documentos: db.getCollection(c).countDocuments() }));

// 2. Usuarios y roles (sin contrasenas ni datos de recuperacion)
db.usuarios.find({}, { _id: 0, nombre: 1, email: 1, rol: 1, activo: 1, fechaRegistro: 1 }).sort({ rol: 1, nombre: 1 });

// 3. Clientes activos
db.clientes.find({ activo: true }, { _id: 0, nombre: 1, cedula: 1, telefono: 1, email: 1, direccion: 1 }).sort({ nombre: 1 });

// 4. Mascotas con el nombre de su propietario (referencia propietarioId -> clientes)
db.mascotas.aggregate([
  { $match: { activo: true } },
  { $lookup: { from: 'clientes', let: { id: { $toObjectId: '$propietarioId' } }, pipeline: [{ $match: { $expr: { $eq: ['$_id', '$$id'] } } }], as: 'propietario' } },
  { $project: { _id: 0, nombre: 1, especie: 1, raza: 1, sexo: 1, peso: 1, fechaNacimiento: 1, propietario: { $first: '$propietario.nombre' } } },
  { $sort: { nombre: 1 } },
]);

// 5. Historial clinico de una mascota (cambiar 'Max' por el nombre deseado)
const mascota = db.mascotas.findOne({ nombre: 'Max' });
db.atenciones.find({ mascotaId: mascota._id.toString() }, { _id: 0, fecha: 1, tipo: 1, descripcion: 1, observaciones: 1, profesional: 1, proximaFecha: 1 }).sort({ fecha: -1 });

// 6. Proximas vacunas y desparasitaciones
db.atenciones.find({ tipo: { $in: ['VACUNA', 'DESPARASITACION'] }, proximaFecha: { $gte: new Date() } }, { _id: 0, mascotaId: 1, tipo: 1, descripcion: 1, proximaFecha: 1 }).sort({ proximaFecha: 1 });

// 7. Citas pendientes de confirmar
db.citas.find({ estado: 'PENDIENTE' }, { _id: 0, inicio: 1, fin: 1, mascotaNombre: 1, clienteNombre: 1, servicioNombre: 1, agenda: 1 }).sort({ inicio: 1 });

// 8. Agenda de los proximos 7 dias (todas las citas activas)
db.citas.find(
  { inicio: { $gte: new Date(), $lt: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000) }, bloqueaAgenda: true },
  { _id: 0, inicio: 1, mascotaNombre: 1, servicioNombre: 1, estado: 1, veterinarioNombre: 1 },
).sort({ inicio: 1 });

// 9. Citas por estado
db.citas.aggregate([{ $group: { _id: '$estado', total: { $sum: 1 } } }, { $sort: { total: -1 } }]);

// 10. Servicios disponibles
db.servicios.find({ activo: true }, { _id: 0, nombre: 1, categoria: 1, precio: 1, duracionMinutos: 1 }).sort({ categoria: 1, nombre: 1 });

// 11. Productos y existencias
db.productos.find({}, { _id: 0, nombre: 1, categoria: 1, precio: 1, stock: 1, stockMinimo: 1, requiereFormula: 1, activo: 1 }).sort({ categoria: 1, nombre: 1 });

// 12. Alertas de stock bajo (stock menor al minimo, HU-15)
db.productos.find({ activo: true, $expr: { $lt: ['$stock', '$stockMinimo'] } }, { _id: 0, nombre: 1, stock: 1, stockMinimo: 1 });

// 13. Ultimos movimientos de inventario
db.movimientos_inventario.find({}, { _id: 0, fecha: 1, productoNombre: 1, tipo: 1, cantidad: 1, saldo: 1, motivo: 1, usuarioNombre: 1 }).sort({ fecha: -1 }).limit(20);

// 14. Pedidos con sus detalles
db.pedidos.find({}, { _id: 0, numero: 1, fecha: 1, clienteNombre: 1, estado: 1, modalidad: 1, direccionEntrega: 1, total: 1, detalles: 1 }).sort({ fecha: -1 });

// 15. Ventas por estado de pedido
db.pedidos.aggregate([{ $group: { _id: '$estado', pedidos: { $sum: 1 }, total: { $sum: '$total' } } }, { $sort: { total: -1 } }]);

// 16. Productos mas vendidos (pedidos no cancelados)
db.pedidos.aggregate([
  { $match: { estado: { $ne: 'CANCELADO' } } },
  { $unwind: '$detalles' },
  { $group: { _id: '$detalles.nombreProducto', unidades: { $sum: '$detalles.cantidad' }, ventas: { $sum: '$detalles.subtotal' } } },
  { $sort: { unidades: -1 } },
]);

// 17. Indices de cada coleccion (unicidad y concurrencia)
db.getCollectionNames().sort().map((c) => ({ coleccion: c, indices: db.getCollection(c).getIndexes().map((i) => ({ nombre: i.name, campos: i.key, unico: !!i.unique, parcial: i.partialFilterExpression || null })) }));
