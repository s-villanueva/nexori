--- añadir un contrato con una empresa y el detalle del contrato -> verificar contratos activos con la empresa
CREATE OR REPLACE PROCEDURE SP_AGREGAR_CONTRATO_EMPRESA_DETALLE(
    P_NOMBRE_EMPRESA VARCHAR,
    P_NOMBRE_PROVEEDOR VARCHAR,
    P_NOMBRE_REGLA VARCHAR,
    P_VIGENTE_DESDE TIMESTAMP,
    P_VIGENTE_HASTA TIMESTAMP,
    P_DETALLES JSONB
)
AS $$
DECLARE
    V_ID_EMPRESA UUID;
    V_ID_EMPRESA_PROVEEDORA UUID;
    V_ID_PROVEEDOR UUID;
    V_ID_REGLA UUID;
    V_ID_CONTRATO_ACTIVO UUID;
    V_ID_CONTRATO_NUEVO UUID;
    V_CONTRATO_ACTIVO BOOLEAN := FALSE;
    V_DETALLES JSONB;
BEGIN
    -- verificar proveedor
    SELECT id_empresa
    INTO V_ID_EMPRESA_PROVEEDORA
    FROM empresa
    WHERE LOWER(nombre) = LOWER(P_NOMBRE_EMPRESA);

    IF NOT FOUND THEN
        RAISE EXCEPTION 'La empresa % no existe', P_NOMBRE_EMPRESA;
    END IF;

    SELECT p.id_proveedor
    INTO V_ID_PROVEEDOR
    FROM proveedor p
             JOIN empresa e
    ON e.id_empresa = p.id_empresa
    WHERE LOWER(e.nombre) = LOWER(P_NOMBRE_PROVEEDOR);

    IF NOT FOUND THEN
        RAISE EXCEPTION 'El proveedor % no existe', P_NOMBRE_PROVEEDOR;
    END IF;

    -- verificar tarifa
    SELECT tr.id_tarifa
    INTO V_ID_REGLA
    FROM tarifa_regla tr
    WHERE LOWER(tr.nombre) = LOWER(P_NOMBRE_REGLA)
      AND tr.id_proveedor = V_ID_PROVEEDOR;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'La regla % no existe o no pertenece al proveedor %',
            P_NOMBRE_REGLA,
            P_NOMBRE_PROVEEDOR;
    END IF;

    -- verificar que no hayan contratos activos
    SELECT cet.id_contrato
    INTO V_ID_CONTRATO_ACTIVO
    FROM contrato_empresa_tarifas cet
    WHERE cet.id_empresa = V_ID_EMPRESA
      AND cet.id_proveedor = V_ID_PROVEEDOR
      AND cet.activo = TRUE
      AND (
        cet.vigente_hasta IS NULL
            OR cet.vigente_hasta >= CURRENT_TIMESTAMP
        )
    LIMIT 1;

    IF V_ID_CONTRATO_ACTIVO IS NOT NULL THEN
        RAISE WARNING 'Ya existe un contrato activo para la empresa % con el proveedor %. Contrato activo: %',
            P_NOMBRE_EMPRESA,
            P_NOMBRE_PROVEEDOR,
            V_ID_CONTRATO_ACTIVO;
        V_CONTRATO_ACTIVO = TRUE;
    END IF;

    -- verificar que no haya un detalle con ese sku
    FOR V_DETALLES IN SELECT * FROM JSONB_array_elements(P_DETALLES)
        LOOP
            IF V_CONTRATO_ACTIVO = TRUE THEN
                IF EXISTS (
                    SELECT 1
                    FROM contrato_empresa_detalle ced
                    INNER JOIN contrato_empresa_tarifas cet2
                        ON ced.id_contrato = cet2.id_contrato
                    WHERE cet2.id_contrato = V_ID_CONTRATO_ACTIVO
                        AND cet2.id_proveedor = V_ID_PROVEEDOR
                        AND cet2.id_empresa = V_ID_EMPRESA
                        AND (ced.sku IS NULL
                            OR (V_DETALLES->>'sku') = ced.sku)
                ) THEN
                    RAISE EXCEPTION 'Ya existe un detalle del contrato que incluye el producto %.', (V_DETALLES->>'sku');
                END IF;
            END IF;
    END LOOP;

    -- verificar vigencias del contrato
    IF V_CONTRATO_ACTIVO != TRUE THEN
        IF P_VIGENTE_HASTA IS NOT NULL
            AND P_VIGENTE_HASTA <= P_VIGENTE_DESDE THEN
            RAISE EXCEPTION 'La fecha vigente_hasta debe ser mayor que vigente_desde';
        END IF;

        IF P_DETALLES IS NULL OR jsonb_array_length(P_DETALLES) = 0 THEN
            RAISE EXCEPTION 'Debe enviar al menos un detalle para el contrato';
        END IF;
    END IF;

    -- verificar los porcentajes
    IF EXISTS (
        SELECT 1
        FROM jsonb_array_elements(P_DETALLES) AS detalle
        WHERE (detalle->>'porcentaje_descuento')::DECIMAL(14,2) < 0
           OR (detalle->>'porcentaje_descuento')::DECIMAL(14,2) > 100
    ) THEN
        RAISE EXCEPTION 'El porcentaje de descuento no puede ser negativo o mayor a 100';
    END IF;

    -- verificar que existan o no los productos
    IF EXISTS (
        SELECT 1
        FROM jsonb_array_elements(P_DETALLES) AS detalle
                 LEFT JOIN producto p
                           ON p.sku = detalle->>'sku'
        WHERE (detalle->>'sku') IS NOT NULL
          AND (detalle->>'sku') <> ''
          AND p.sku IS NULL
    ) THEN
        RAISE EXCEPTION 'Uno o más productos del detalle no existen';
    END IF;

    -- insertar contrato
    IF V_CONTRATO_ACTIVO != TRUE THEN
        INSERT INTO contrato_empresa_tarifas (id_empresa, id_proveedor, id_regla, vigente_desde,
                                              vigente_hasta, activo
        ) VALUES (V_ID_EMPRESA, V_ID_PROVEEDOR, V_ID_REGLA, P_VIGENTE_DESDE,
               P_VIGENTE_HASTA,TRUE
        )
        RETURNING id_contrato INTO V_ID_CONTRATO_ACTIVO;
    END IF;

    -- insertar detalle del contrato
    INSERT INTO contrato_empresa_detalle (
            porcentaje_descuento,
            sku,
            id_contrato
        )
        SELECT
            (detalles->>'porcentaje_descuento')::DECIMAL(14,2),
             detalles->>'sku',
            V_ID_CONTRATO_ACTIVO
        FROM jsonb_array_elements(P_DETALLES) AS detalles;
        RAISE INFO 'Contrato creado correctamente. ID contrato: %', V_ID_CONTRATO_ACTIVO;
END;
$$ LANGUAGE plpgsql;
