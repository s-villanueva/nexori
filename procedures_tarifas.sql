-- añadir una regla de tarifa y sus tramos -> verificar que no exista una regla de tarifa con ese nombre del mismo proveedor
CREATE OR REPLACE PROCEDURE SP_AGREGAR_REGLA_TARIFA_CON_TRAMOS(
    P_NOMBRE VARCHAR, -- del producto
    P_DESCRIPCION TEXT,
    P_NOMBRE_PROVEEDOR VARCHAR,
    P_TRAMOS JSONB
)
AS $$
DECLARE
    V_ID_EMPRESA UUID;
    V_ID_PROVEEDOR UUID;
    V_ID_REGLA_EXISTENTE UUID;
    V_ID_REGLA_NUEVA UUID;
BEGIN
    SELECT id_empresa INTO V_ID_EMPRESA
    FROM empresa
    WHERE LOWER(nombre) = LOWER(P_NOMBRE_PROVEEDOR);

    IF NOT FOUND THEN
        RAISE EXCEPTION 'No existe empresa con nombre %', P_NOMBRE_PROVEEDOR;
    END IF;

    SELECT id_proveedor INTO V_ID_PROVEEDOR
    FROM proveedor
    WHERE id_empresa = V_ID_EMPRESA;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'No existe proveedor asociado a la empresa %', P_NOMBRE_PROVEEDOR;
    END IF;

    SELECT id_tarifa
    INTO V_ID_REGLA_EXISTENTE
    FROM tarifa_regla
    WHERE LOWER(nombre) = LOWER(P_NOMBRE)
      AND id_proveedor = V_ID_PROVEEDOR
    LIMIT 1;

    IF V_ID_REGLA_EXISTENTE IS NOT NULL THEN
        RAISE EXCEPTION 'Ya existe una regla de tarifa con el nombre % para este proveedor', P_NOMBRE;
    END IF;

    IF P_TRAMOS IS NULL OR jsonb_array_length(P_TRAMOS) = 0 THEN
        RAISE EXCEPTION 'Debe enviar al menos un tramo';
    END IF;

    -- tipo obligatorio
    IF EXISTS (
        SELECT 1
        FROM jsonb_array_elements(P_TRAMOS) t
        WHERE t->>'tipo' IS NULL OR t->>'tipo' = ''
    ) THEN
        RAISE EXCEPTION 'Todos los tramos deben tener tipo';
    END IF;

    -- tipo válido
    IF EXISTS (
        SELECT 1
        FROM jsonb_array_elements(P_TRAMOS) t
        WHERE t->>'tipo' NOT IN ('volumen', 'costo')
    ) THEN
        RAISE EXCEPTION 'Tipo inválido (solo volumen o costo)';
    END IF;

    -- cantidad mínima válida
    IF EXISTS (
        SELECT 1
        FROM jsonb_array_elements(P_TRAMOS) t
        WHERE t->>'cantidad_minima' IS NULL
           OR t->>'cantidad_minima' = ''
           OR (t->>'cantidad_minima')::DECIMAL(14,2) < 0
    ) THEN
        RAISE EXCEPTION 'cantidad_minima inválida';
    END IF;

    -- cantidad máxima > mínima
    IF EXISTS (
        SELECT 1
        FROM jsonb_array_elements(P_TRAMOS) t
        WHERE t->>'cantidad_maxima' IS NOT NULL
          AND t->>'cantidad_maxima' <> ''
          AND (t->>'cantidad_maxima')::DECIMAL(14,2)
            <= (t->>'cantidad_minima')::DECIMAL(14,2)
    ) THEN
        RAISE EXCEPTION 'cantidad_maxima debe ser mayor a cantidad_minima';
    END IF;

    -- porcentaje válido
    IF EXISTS (
        SELECT 1
        FROM jsonb_array_elements(P_TRAMOS) t
        WHERE t->>'porcentaje_desc' IS NULL
           OR t->>'porcentaje_desc' = ''
           OR (t->>'porcentaje_desc')::DECIMAL(14,2) < 0
           OR (t->>'porcentaje_desc')::DECIMAL(14,2) > 100
    ) THEN
        RAISE EXCEPTION 'porcentaje_desc inválido';
    END IF;

    IF EXISTS (
        WITH tramos AS (
            SELECT
                ord,
                t->>'tipo' AS tipo,
                (t->>'cantidad_minima')::DECIMAL(14,2) AS min,
                NULLIF(t->>'cantidad_maxima','')::DECIMAL(14,2) AS max
            FROM jsonb_array_elements(P_TRAMOS) WITH ORDINALITY AS x(t, ord)
        )
        SELECT 1
        FROM tramos a
                 JOIN tramos b
                      ON a.ord < b.ord
                          AND a.tipo = b.tipo
        WHERE a.min <= COALESCE(b.max, 999999999999)
          AND COALESCE(a.max, 999999999999) >= b.min
    ) THEN
        RAISE EXCEPTION 'Tramos sobrepuestos';
    END IF;

    INSERT INTO tarifa_regla (
        nombre,
        descripcion,
        id_proveedor,
        activo
    )
    VALUES (
               P_NOMBRE,
               P_DESCRIPCION,
               V_ID_PROVEEDOR,
               TRUE
           )
    RETURNING id_tarifa INTO V_ID_REGLA_NUEVA;

    INSERT INTO tramo_tarifa (
        tipo,
        cantidad_minima,
        cantidad_maxima,
        porcentaje_desc,
        id_regla
    )
    SELECT
        t->>'tipo',
        (t->>'cantidad_minima')::DECIMAL(14,2),
        NULLIF(t->>'cantidad_maxima','')::DECIMAL(14,2),
        (t->>'porcentaje_desc')::DECIMAL(14,2),
        V_ID_REGLA_NUEVA
    FROM jsonb_array_elements(P_TRAMOS) t;

    RAISE INFO 'Regla creada correctamente. ID: %', V_ID_REGLA_NUEVA;

END;
$$ LANGUAGE plpgsql;