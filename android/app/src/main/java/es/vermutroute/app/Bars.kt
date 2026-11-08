package es.vermutroute.app

/** An opening slot in decimal hours; 26.0 means 02:00 of the next day. */
typealias Slot = Pair<Double, Double>

data class Bar(
    val id: String,
    val name: String,
    val address: String,
    /** What the geocoder searches for. */
    val query: String,
    val zone: String,
    val fact: String,
    /** One entry per weekday, Monday first. Empty = closed. Null = no data. */
    val hours: List<List<Slot>?>,
    val note: String? = null,
)

private fun r(vararg v: Double): List<Slot> = v.toList().chunked(2).map { it[0] to it[1] }
private val C: List<Slot> = emptyList()

/**
 * The 14 bars of the route: the official map of the Academia Madrileña de
 * Gastronomía without Casa Maravillas (permanently closed). Hours checked on
 * Google Maps on 8 Nov 2026 (Bodegas Alfaro: esMADRID, 2023). See HORARIOS.md.
 */
val BARS: List<Bar> = listOf(
    Bar(
        "ardosa", "Bodega de la Ardosa", "C/ Colón, 13", "Calle de Colón 13, 28004 Madrid, España", "Recoletos",
        "Abrió en 1892 y conserva su barra de madera y su decoración antigua. Además del vermut de grifo, es famosa por su tortilla de patata y sus salazones.",
        listOf(r(9.0, 26.0), r(9.0, 26.0), r(9.0, 26.0), r(9.0, 26.0), r(9.0, 26.5), r(9.0, 26.5), r(9.0, 26.0)),
    ),
    Bar(
        "camacho", "Casa Camacho “Yayos”", "C/ San Andrés, 4", "Calle de San Andrés 4, 28004 Madrid, España", "Universidad",
        "Aquí nació el «yayo»: vermut, ginebra y gaseosa. Lleva tirando vermut de grifo desde los años 20.",
        listOf(r(12.5, 26.0), r(12.5, 26.0), C, r(12.5, 26.0), r(12.5, 26.0), r(12.5, 26.0), r(12.5, 26.0)),
        note = "Google avisa de que puede estar cerrado: confírmalo",
    ),
    Bar(
        "angel", "Taberna Ángel Sierra", "C/ Gravina, 11", "Calle de Gravina 11, 28004 Madrid, España", "Chueca",
        "Conserva el vermut de grifo y su barra de roble, entre botellas antiguas.",
        listOf(r(12.0, 26.5), r(12.0, 26.5), r(12.0, 26.5), r(12.0, 26.5), r(12.0, 26.5), r(12.0, 26.5), r(12.0, 25.0)),
    ),
    Bar(
        "alberto", "Casa Alberto", "C/ de las Huertas, 18", "Calle de las Huertas 18, 28012 Madrid, España", "Las Letras",
        "Se fundó en 1827, es «Establecimiento Centenario» y está en pleno Barrio de las Letras, cargado de memoria literaria.",
        listOf(C, r(12.0, 23.0), r(12.0, 23.0), r(12.0, 23.0), r(12.0, 23.0), r(12.0, 23.0), r(12.0, 16.0)),
    ),
    Bar(
        "anciano", "El Anciano Rey de los Vinos", "C/ de Bailén, 19", "Calle de Bailén 19, 28005 Madrid, España", "Palacio",
        "Está frente al Palacio Real y sirve vinos y vermut de grifo en su barra de azulejo desde principios del siglo XX.",
        listOf(r(9.5, 23.5), C, r(9.5, 23.5), r(9.0, 23.5), r(9.5, 23.5), r(9.5, 23.5), r(9.5, 23.5)),
    ),
    Bar(
        "rosell", "Bodegas Rosell", "C/ del General Lacy, 14", "Calle del General Lacy 14, 28045 Madrid, España", "Delicias",
        "Abrió en 1920 junto a Atocha. Su fachada de azulejos es obra de Alfonso Romero, y todavía sirve vermut de barril.",
        listOf(C, r(12.0, 16.0, 19.0, 24.0), r(12.0, 16.0, 19.0, 24.0), r(12.0, 16.0, 19.0, 24.0), r(12.0, 16.0, 19.0, 24.0), r(12.0, 16.0, 19.5, 24.0), r(12.0, 16.0)),
    ),
    Bar(
        "casas", "Bodegas Casas", "Av. Ciudad de Barcelona, 23", "Avenida de la Ciudad de Barcelona 23, 28007 Madrid, España", "Pacífico",
        "Es centenaria: abrió en 1923 y mantiene su barra de época. Tiene dos locales en la misma avenida.",
        listOf(r(11.0, 24.0), r(11.0, 24.0), r(11.0, 24.0), r(11.0, 24.0), r(11.0, 24.0), r(11.0, 24.0), r(11.0, 23.0)),
    ),
    Bar(
        "violeta", "La Violeta Vermut", "C/ de Vallehermoso, 62", "Calle de Vallehermoso 62, 28015 Madrid, España", "Vallehermoso",
        "Tiene más de 30 referencias de vermut: es el sitio para descubrir perfiles distintos.",
        listOf(C, r(19.0, 25.0), r(19.0, 25.0), r(19.0, 25.0), r(13.0, 26.0), r(12.5, 26.0), r(12.5, 16.5)),
    ),
    Bar(
        "sifon", "Sifón Bodega Colmado", "Plaza del Rey, 4", "Plaza del Rey 4, 28004 Madrid, España", "Chueca",
        "Es bodega y colmado a la vez: vermut bien servido con conservas de calidad, y una terraza muy codiciada.",
        listOf(C, r(11.0, 23.5), r(11.0, 23.5), r(11.0, 24.0), r(11.0, 24.5), r(11.5, 24.5), r(11.5, 21.5)),
    ),
    Bar(
        "alfaro", "Bodegas Alfaro", "C/ Ave María, 10", "Calle del Ave María 10, 28012 Madrid, España", "Lavapiés",
        "Es una bodega mítica de 1929, con vermut de grifo, barra de zinc y salazones.",
        listOf(r(13.0, 16.0, 20.0, 24.0), r(13.0, 16.0, 20.0, 24.0), r(13.0, 16.0, 20.0, 24.0), r(13.0, 16.0, 19.0, 24.0), r(13.0, 16.0, 19.0, 25.0), r(13.0, 25.0), r(13.0, 22.0)),
        note = "Horario de esMADRID (2023): Google no lo tiene",
    ),
    Bar(
        "mano", "Bodegas El Maño", "C/ de la Palma, 64", "Calle de la Palma 64, 28015 Madrid, España", "Universidad",
        "Lleva desde 1927 junto a Conde Duque, sirviendo vermut con raciones clásicas como croquetas o bravas.",
        List(7) { r(12.5, 25.0) },
    ),
    Bar(
        "flaca", "La Flaca Taberna", "C/ de Serrano, 43", "Calle de Serrano 43, 28001 Madrid, España", "Salamanca",
        "Los domingos tiene barra libre de vermut a precio fijo.",
        listOf(r(9.0, 24.0), r(9.0, 24.0), r(9.0, 25.0), r(9.0, 25.0), r(9.0, 26.0), r(12.0, 20.5), r(12.0, 24.0)),
    ),
    Bar(
        "cheli", "Taberna Alma Cheli", "C/ San Félix, 9", "Calle de San Félix 9, 28045 Madrid, España", "Arganzuela",
        "Presume de tener «uno de los mejores vermús» de la capital.",
        listOf(r(13.0, 17.0, 20.0, 23.5), r(13.0, 16.5, 20.0, 23.5), C, r(12.0, 16.5, 20.0, 23.5), r(12.0, 17.0, 20.0, 24.0), r(12.0, 16.5, 19.5, 24.0), r(12.0, 17.0, 19.5, 24.0)),
    ),
    Bar(
        "vinagre", "Hermanos Vinagre", "C/ Argumosa, 12", "Calle de Argumosa 12, 28012 Madrid, España", "Lavapiés",
        "Es un templo del aperitivo castizo: conservas, encurtidos y vermut bien servido. Tiene locales en Cardenal Cisneros, Narváez y Argumosa.",
        listOf(r(12.0, 16.0, 19.0, 22.5), r(12.0, 16.0, 19.0, 22.5), r(12.0, 16.0, 19.0, 22.5), r(12.0, 16.0, 19.0, 22.5), r(12.0, 23.5), r(12.0, 23.5), r(12.0, 23.5)),
    ),
)

enum class OpenStatus { OPEN, CLOSED, UNKNOWN }

/** weekday: 0 = Monday … 6 = Sunday. */
fun Bar.statusAt(weekday: Int, hour: Double): OpenStatus {
    val day = hours.getOrNull(weekday) ?: return OpenStatus.UNKNOWN
    if (day.isEmpty()) return OpenStatus.CLOSED
    // Also count the after-midnight tail of the previous day (e.g. open until 02:00).
    val prev = hours.getOrNull((weekday + 6) % 7)
    val openToday = day.any { hour >= it.first && hour < it.second }
    val openFromYesterday = prev?.any { it.second > 24.0 && hour + 24.0 < it.second } ?: false
    return if (openToday || openFromYesterday) OpenStatus.OPEN else OpenStatus.CLOSED
}

val DAY_NAMES = listOf("lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo")
