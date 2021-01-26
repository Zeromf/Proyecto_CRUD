@RequestMapping(value = "/administrarPromociones.htm")
	ModelAndView administrarPromociones(@RequestParam(defaultValue = "1") Integer page,
			@RequestParam(required = false, value = "nombre") String nombre,
			@RequestParam(required = false, value = "condicion") Long idCondicion,
			@RequestParam(defaultValue = "", value = "fechaDesde") String fechaDesde,
			@RequestParam(defaultValue = "", value = "fechaHasta") String fechaHasta,
//			@RequestParam(defaultValue = "", value = "entidadcreacion") String entidadcreacion,
			@RequestParam(required = false, value = "codigoJerarquico") String codigoJerarquico, Principal principal) {
		
		Map<String, Object> modelo = new HashMap<String, Object>();

		Criterion criterion = Restrictions.ge("id", 1L);
		if (nombre != null && !nombre.isEmpty())
			criterion = Restrictions.and(criterion, Restrictions.ilike("nombre", "%" + nombre + "%"));
		if (idCondicion != null)
			criterion = Restrictions.and(criterion, Restrictions.eq("condicion.id", idCondicion));
		if (codigoJerarquico != null && !codigoJerarquico.isEmpty())
			criterion = Restrictions.and(criterion, Restrictions.ilike("codigoJerarquico", "%" + codigoJerarquico + "%"));
		
//		if (entidadcreacion != null )
//			criterion = Restrictions.and(criterion, Restrictions.ilike("entidadcreacion", "%" + entidadcreacion + "%"));
		
		

		if (!fechaDesde.equals("") && (fechaHasta == null || (fechaHasta != null && fechaHasta.equals("")))) {
			Calendar fechaDesdeMasUno = Calendar.getInstance();
			fechaDesdeMasUno.setTime(FechaHelper.convertirFechaADate(fechaDesde));
			fechaDesdeMasUno.set(Calendar.HOUR, 0);
			fechaDesdeMasUno.set(Calendar.MINUTE, 0);
			fechaDesdeMasUno.set(Calendar.SECOND, 0);
			fechaDesdeMasUno.add(Calendar.DATE, 1);
			fechaDesdeMasUno.add(Calendar.MILLISECOND, -1);
			criterion = Restrictions.and(criterion,
					Restrictions.between("fechaDesde", FechaHelper.convertirFechaADate(fechaDesde), fechaDesdeMasUno.getTime()));
		}

		if ((fechaHasta != null && !fechaHasta.equals(""))
				&& (fechaHasta == null || (fechaHasta != null && fechaHasta.equals("")))) {
			Calendar fechaHastaMasUno = Calendar.getInstance();
			fechaHastaMasUno.setTime(FechaHelper.convertirFechaADate(fechaHasta));
			fechaHastaMasUno.add(Calendar.DATE, 1);
			criterion = Restrictions.and(criterion,
					Restrictions.between("fechaHasta", FechaHelper.convertirFechaADate(fechaHasta), fechaHastaMasUno.getTime()));
		}
		if (!fechaDesde.equals("") && (fechaHasta != null && !fechaHasta.equals(""))) {
			Calendar fechaHastaMasUno = Calendar.getInstance();
			fechaHastaMasUno.setTime(FechaHelper.convertirFechaADate(fechaHasta));
			fechaHastaMasUno.add(Calendar.DATE, 1);
			criterion = Restrictions.and(criterion,
					Restrictions.between("fechaDesde", FechaHelper.convertirFechaADate(fechaDesde), fechaHastaMasUno.getTime()));
		}
		
		EntidadDatafarma entidadDatafarma = entidadDatafarmaServiceManager.obtenerEntidadDatafarma(principal);

		if (entidadDatafarma.getTipoEntidad().getId().equals(TipoEntidadDatafarma.LABORATORIO)) {

			EntidadLaboratorio laboratorio = (EntidadLaboratorio) entidadDatafarma;
			Boolean tieneDistribuidor = laboratorio.getEntidadesDistribuidores().isEmpty();
			modelo.put("tieneDistribuidor", tieneDistribuidor);
		}

		if (!EntidadAdministrador.class.isInstance(entidadDatafarma)) {
			String codigoJerarquicoUsuarioLogueado = entidadDatafarma.getCodigoJerarquico();
			criterion = Restrictions.and(criterion, Restrictions.ilike("codigoJerarquico", codigoJerarquicoUsuarioLogueado));

		} else if (entidadDatafarma instanceof EntidadFarmacia)
			entidadDatafarma = (EntidadFarmacia) entidadDatafarma;

		Integer pageSize = Integer.parseInt(SpringPropertiesUtil.getProperty("app.datafarmaCentro.pageSize"));
		Integer from = (page - 1) * pageSize;
		List<Promocion> promociones = promocionManager.find(criterion, from, pageSize, "fechaDesde", "desc");
		Long count = promocionManager.count(criterion);

		modelo.put("promociones", promociones);
		modelo.put("nombre", nombre);
		modelo.put("fechaDesde", fechaDesde);
		modelo.put("fechaHasta", fechaHasta);
	//	modelo.put("entidadCreacion", entidadcreacion);
		modelo.put("farmaciaLogueada", entidadDatafarma);

		// DATOS PAGINACION
		modelo.put("pageSize", pageSize);
		modelo.put("totalPages", (int) Math.ceil(count.floatValue() / pageSize.floatValue()));
		modelo.put("page", page);
		
		modelo.put("jsp", "administrarPromociones.htm");

		modelo.put("condiciones", condicionManager.list("nombre", "asc"));

		return new ModelAndView("abmPromocion", "modelo", modelo);
	}
