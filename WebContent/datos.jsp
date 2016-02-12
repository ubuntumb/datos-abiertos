<%@page import="java.io.ByteArrayOutputStream"%>
<%@page import="data.open.py.util.ResponseWrapper"%>
<%@page import="java.util.ArrayList"%>
<%@page import="data.open.py.util.ParamWrapper"%>
<%@page import="data.open.py.util.CommonClass"%>
<%@page import="data.open.py.entidad.Multa"%>
<%@page import="java.util.*"%>
<%@page import="com.fasterxml.jackson.databind.ObjectMapper" %>
<%@page import="org.codehaus.jackson.*" %>

<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<jsp:useBean id="commonClass" class="data.open.py.util.CommonClass"></jsp:useBean>
<jsp:useBean id="multaService" class="data.open.py.service.MultasService"></jsp:useBean>

<%
  String id_multa = request.getParameter("multa") != null ? request.getParameter("multa") : null;
  String codigo_sancion = request.getParameter("cod_sanc") != null ? request.getParameter("cod_sanc") : null;
  String descripcion = request.getParameter("descrip") != null ? request.getParameter("descrip") : null;
  String tipo_vehiculo = request.getParameter("tipoVehi") != null ? request.getParameter("tipoVehi") : null;
  String fecha_sancion = request.getParameter("fecha_sanc") != null ? request.getParameter("fecha_sanc") : null;
  String hora_sancion = request.getParameter("hora_sanc") != null ? request.getParameter("hora_sanc") : null;
  String monto = request.getParameter("monto") != null ? request.getParameter("monto") : null;;
  String estado_multa = request.getParameter("est_multa") != null ? request.getParameter("est_multa") : null;;
  String fecha_cobro = request.getParameter("fech_cobro") != null ? request.getParameter("fech_cobro") : null;
  String ciudad_registro_conducir = request.getParameter("ciudad_reg") != null ? request.getParameter("ciudad_reg") : null;
  String departamento_registro_conducir = request.getParameter("depart_reg") != null ? request.getParameter("ciudad_reg") : null;
  String pagina = request.getParameter("pag") != null ? request.getParameter("pag") : "1";
  String limite = request.getParameter("limit") != null ? request.getParameter("limit") : "10";
  String cantida_reg = request.getParameter("cant_reg") != null ? request.getParameter("cant_reg") : null;
  String cantidad_pag = request.getParameter("cant_pag") != null ? request.getParameter("cant_pag") : null;
  
  ParamWrapper params = new ParamWrapper();
  String query = "SELECT COUNT(*) FROM multas_mopc";

	Map<String,Integer> map = CommonClass.pagination(query,params.getLimite());
	
	params.setCantida_reg(map.get("cant_reg"));
	params.setId_Multa(id_multa);
	params.setPagina(Integer.parseInt(pagina));
	params.setCantidad_pag(map.get("cant_pag"));

%>
	<% 
		ArrayList<Multa> list =  multaService.getListMultas(params);
	    ResponseWrapper respon = new ResponseWrapper();
	    respon.setCanPag(params.getCantidad_pag());
	    respon.setCanReg(params.getCantida_reg());
	    respon.setPag(params.getPagina());
	    Multa[] multas = list.toArray(new Multa[list.size()]);
	 	respon.setMultas(multas);
	 	ObjectMapper mapper = new ObjectMapper();
	 	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	 	mapper.writeValue(bos, respon);
	 	response.setContentType("application/json");
	 	out.write(mapper.writeValueAsString(respon));
	 	
		%>