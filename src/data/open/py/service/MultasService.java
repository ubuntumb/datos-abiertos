package data.open.py.service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import data.open.py.connection.Conexion;
import data.open.py.entidad.Distrito;
import data.open.py.entidad.Estado;
import data.open.py.entidad.Multa;
import data.open.py.util.CommonClass;
import data.open.py.util.ParamWrapper;
import data.open.py.util.ResponseWrapper;

/**
 * 
 * @author mbenitez
 * Clase encargada de proveer servicios comunes para la aplicacion <br>
 */
public class MultasService {
	private StringBuffer buffer;
	private ArrayList<Multa> multas;

	public MultasService() {
		buffer = new StringBuffer();
		multas = new ArrayList<>();

		buffer.append("SELECT " + " id_multa, " + " codigo_sancion, "
				+ " descripcion, " + " tipo_vehiculo, " + " fecha_sancion, "
				+ " hora_sancion, " + " monto, " + " estado_multa, "
				+ " fecha_cobro, " + " ciudad_registro_conducir,"
				+ " departamento_registro_conducir, " + " destacamento "
				+ " FROM multas_mopc ");
	}
/**
 * Método que provee una lista de Multas <br> recibe como parametro los 
 * datos del request y dos String mas para crear un orden y la direccion del orden <br>
 * de la consulta a crear y proveer la lista en base a los parametros
 * @param params
 * @param ordCol
 * @param ordDir
 * @return ArrayList<Multa>
 */
	public ArrayList<Multa> getListMultas(ParamWrapper params,String ordCol, String ordDir) {
		Statement statm;
		ResultSet result;
		String where = CommonClass.getFilterFromParams(params);
		Integer ordCols = ordCol.equals("") ? 1 : Integer.parseInt(ordCol) + 1 ;
		ordDir = ordDir.equals("") ? "desc" : ordDir; 
		ArrayList<Multa> multas = new ArrayList<>();

		if (!where.equals("")) {
			buffer.append(" where ");
			buffer.append(where);
		}

		buffer.append(" order by "+ordCols+" "+ordDir);
		buffer.append(" limit " + params.getLimite());
		buffer.append(" offset " + params.getPagina());

		try {

			statm = Conexion.getConnection().createStatement();
			System.out.println("sql: " + buffer.toString());
			result = statm.executeQuery(buffer.toString());

			while (result.next()) {
				Multa multa = new Multa();
				multa.setIdMulta(result.getString(1));
				multa.setCodigoSancion(result.getString(2));
				multa.setDescripcion(result.getString(3));
				multa.setTipoVehiculo(result.getString(4));
				multa.setFechaSancion(result.getDate(5));
				multa.setHoraSancion(result.getString(6));
				multa.setMonto(result.getInt(7));
				multa.setEstadoMulta(result.getString(8));
				multa.setFechaCobro(result.getDate(9));
				multa.setCiudadRegistroConducir(result.getString(10));
				multa.setDepartamentoRegistroConducir(result.getString(11));
				multa.setDestacamento(result.getString(12));

				multas.add(multa);

			}

		} catch (Exception e) {

		}

		return multas;
	}
/**
 * Metodo que devuelve un String utilizado para filtrar datos, crea una <br>
 * cadena con la sentencia <i>where sql</i> 
 * @param params
 * @return StringBuffer
 */
	public StringBuffer getPaginationQuery(ParamWrapper params) {
		StringBuffer query = new StringBuffer();
		query.append("SELECT COUNT(*) FROM multas_mopc ");
		String where = CommonClass.getFilterFromParams(params);
		ArrayList<Multa> multas = new ArrayList<>();

		if (!where.equals("")) {
			query.append(" where ");
			query.append(where);
		}
		System.out.println("sql: " + query.toString());
		return query;
	}
/**
 * Método utilizado para proveer datos sobre multas por departamento y distrito <br>
 * recibe como parametro la descripcion de un departamento
 * @param dpto
 * @return
 */
	public ResponseWrapper getCharData(String dpto) {

		String query1 = ""
				+ " SELECT  "
				+ "			departamento_registro_conducir,"
				+ " 		ciudad_registro_conducir,"
				+ "			estado_multa, "
				+ " 		COUNT(ciudad_registro_conducir) as cantidad, "
				+ "			SUM(monto) as monto_estados"
				+ " FROM multas_mopc "
				+ " WHERE departamento_registro_conducir not ilike '%DEL SUR%' "
				+ " and unaccent(departamento_registro_conducir) = upper(unaccent(?)) "
				+ " GROUP BY departamento_registro_conducir,ciudad_registro_conducir ,estado_multa "
				+ " order by 1,2 desc";
		PreparedStatement preSta;
		List<Registro> registros = new ArrayList<>();
		try {

			preSta = Conexion.getConnection().prepareStatement(query1);
			preSta.setString(1, dpto);
			ResultSet rs = preSta.executeQuery();
			while (rs.next()) {
				Registro t = new Registro();
				t.departamento = rs.getString("departamento_registro_conducir");
				t.ciudad = rs.getString("ciudad_registro_conducir");
				t.estado = rs.getString("estado_multa");
				t.cantidad = rs.getInt("cantidad");
				t.monto = rs.getLong("monto_estados");
				registros.add(t);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		ResponseWrapper rw = new ResponseWrapper();
		Set<String> categorias = new HashSet<String>();
		for (Registro r : registros) {
			categorias.add(r.ciudad);
		}
		rw.setCategorias(categorias);
		Map<String, Distrito> distritos = new HashMap<String, Distrito>();

		for (Registro r : registros) {
			Distrito actual;
			if (distritos.containsKey(r.ciudad)) {
				actual = distritos.get(r.ciudad);
			} else {
				actual = new Distrito(r.ciudad);
				distritos.put(r.ciudad, actual);
			}
			Estado e = new Estado();
			e.setName(r.estado);
			e.setData(r.cantidad);
			e.setMonto(r.monto);
			actual.getEstado().add(e);
		}
		
		rw.setSeries(new ArrayList<Distrito>(distritos.values()));

		return rw;
	}

	public static class Registro {
		String departamento;
		String ciudad;
		String estado;
		Integer cantidad;
		Long monto;
	}
}
