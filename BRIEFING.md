# Ruta del Vermut háptica · Briefing

Una ruta a pie por vermuterías de Madrid **sin mapa**: el móvil va en el bolsillo y te guía solo con vibraciones. En cada parada te da un fun fact del bar y una ficha para valorarlo. Al final, un ranking.

Prototipo (canvas de diseño): https://claude.ai/artifact/VPpvtQ1zctbNzwm24ETsSD

Probador háptico para Android: [`docs/index.html`](docs/index.html), que GitHub Pages publica en https://fer-aranda-avila.github.io/vermut-route/ cuando se activa Pages con la carpeta `/docs`.

## Decisiones tomadas

| Tema | Decisión |
| --- | --- |
| Dispositivo | Móvil en el bolsillo. La pantalla solo se usa para empezar, al llegar y para la ficha. |
| Lenguaje háptico | Se prueban tres (A, B y C, abajo) antes de elegir uno. |
| Ruta | La eligen los usuarios: marcan de 3 a 5 bares y la app los ordena. No se avisa de la distancia: el filtro por barrio ya deja claro dónde está cada bar. |
| Modo sorpresa | Se activa en la pantalla de selección de bares, antes de ver la ruta. Oculta el orden y el nombre de cada parada hasta llegar. |
| Horarios | Al planificar se elige día y hora. Si un bar puede estar cerrado a esa hora, o no hay datos, se muestra un aviso, pero se puede seleccionar igualmente. En la ruta, el horario se comprueba a la **hora estimada de llegada a cada bar** (salida + unos 40 min por bar + paseo), porque al último se llega horas después de salir. *Propuesta pendiente de validar.* |
| Fun facts | Uno por bar, sacado de la descripción del plano oficial. |
| Ficha | Una ficha corta tras cada bar y un ranking al final de la ruta. |

## Flujo

1. Bienvenida
2. Elegir día y hora, bares (filtro por barrio, aviso de horario) y modo sorpresa
3. Ruta ordenada y elección del lenguaje háptico
4. Modo bolsillo
5. Llegada con fun fact
6. Ficha de cata
7. Resumen y ranking

En el modo bolsillo hay tres ayudas: **Repetir** (vuelve a vibrar), **Me he perdido** (una pista en texto y, si hace falta, una flecha durante 10 s) y **He llegado** (check-in manual).

## Lenguajes hápticos (patrones en ms, formato `navigator.vibrate`)

Los tres lenguajes comparten el patrón de **llegada**: `[500,120,150,120,500]`, que suena largo · corto · largo.

**A · Frío / caliente.** Late mientras caminas en la dirección buena. Usa el rumbo del GPS o la brújula.
- Vas bien: `[60,120,60]` cada 3 s
- Te desvías: silencio
- Vuelves al rumbo: `[300]`
- A menos de 100 m: `[60,80,60,80,60]`

**B · Giro a giro.** Un patrón unos 30 m antes de cada cruce y silencio el resto del tiempo.
- Izquierda: `[120,150,120]`
- Derecha: `[450]`
- Recto: silencio
- Media vuelta: `[80×5 con pausas de 80]`

**C · Radar de distancia.** Solo indica la distancia.
- Más de 300 m: un latido cada 5 s
- Entre 100 y 300 m: un latido cada 2 s
- Menos de 100 m: latido casi continuo
- Te alejas: `[700]`

## Ficha de cata (criterios genéricos, por personalizar)

- Tipo: rojo, blanco, rosado o reserva
- Escalas de 1 a 5:
  - Aroma
  - Equilibrio dulce/amargo
  - Cuerpo y textura
  - Servicio (temperatura, hielo, naranja)
  - Tapa
  - Calidad/precio
- ¿Repetirías? (Sí, Quizá o No) y notas libres
- Nota media = promedio de las escalas

## Bares de la ruta

Los 14 bares de la ruta: los 15 del plano oficial menos Casa Maravillas, que ha cerrado definitivamente. Alma Cheli se ha mudado a San Félix, 9 y su local de Santa Engracia ya no está. El número es el del plano.

| # plano | Bar | Dirección | Barrio | Metro |
| --- | --- | --- | --- | --- |
| 1 | Bodega de la Ardosa | C/ Colón, 13 | Recoletos | Tribunal · Santo Domingo |
| 2 | Casa Camacho “Yayos” | C/ San Andrés, 4 | Universidad | Tribunal |
| 3 | Taberna Ángel Sierra | C/ Gravina, 11 | Chueca | Tribunal · Santo Domingo |
| 4 | Casa Alberto | C/ de las Huertas, 18 | Las Letras | Antón Martín · Sol |
| 5 | El Anciano Rey de los Vinos | C/ de Bailén, 19 | Palacio | Ópera · La Latina |
| 6 | Bodegas Rosell | C/ del General Lacy, 14 | Delicias | Delicias · Palos de la Frontera |
| 7 | Bodegas Casas | Av. Ciudad de Barcelona, 23 (y otro local en el 57) | Pacífico | Pacífico |
| 8 | La Violeta Vermut | C/ de Vallehermoso, 62 | Vallehermoso | Canal · Quevedo |
| 9 | Sifón Bodega Colmado | Plaza del Rey, 4 | Chueca | Banco de España · Chueca |
| 10 | Bodegas Alfaro | C/ Ave María, 10 | Lavapiés | Lavapiés |
| 11 | Bodegas El Maño | C/ de la Palma, 64 | Universidad | Tribunal · Noviciado |
| ~~12~~ | ~~Casa Maravillas~~ | cerrado definitivamente | | |
| 13 | La Flaca Taberna | C/ de Serrano, 43 | Salamanca | Serrano · Retiro |
| 14 | Taberna Alma Cheli | C/ San Félix, 9 | Arganzuela | Legazpi |
| 15 | Hermanos Vinagre | C/ Argumosa, 12 (también Cardenal Cisneros y Narváez) | Lavapiés | Lavapiés · Antón Martín |

Google Maps confirma Sifón en el número 4, La Violeta en el 62 y La Flaca en el 43. En **Bodegas Casas** y **Hermanos Vinagre**, que tienen varios locales, la ruta usa el del plano (Ciudad de Barcelona, 23 y Argumosa, 12).

## Horarios

Los horarios de cada bar, comprobados en Google Maps el 8 de noviembre de 2026, están en [`HORARIOS.md`](HORARIOS.md). Son los que usa el prototipo para avisar de que un bar puede estar cerrado.

Días de cierre:
- **Lunes:** Casa Alberto, Bodegas Rosell, La Violeta y Sifón.
- **Martes:** El Anciano Rey de los Vinos.
- **Miércoles:** Casa Camacho y Alma Cheli.
- **Domingo por la tarde:** Casa Alberto y Bodegas Rosell (abren solo hasta las 16:00); La Violeta cierra a las 16:30.
- **De martes a jueves, solo por la tarde (desde las 19:00):** La Violeta.

Con dudas:
- **Casa Camacho:** Google avisa de que «puede estar cerrado», aunque tiene reseñas recientes. Además, Time Out decía que cerraba los domingos y Google dice que cierra los miércoles.
- **Bodegas Alfaro:** Google no tiene su horario; el de la tabla es de esMADRID (2023).
- **La Flaca:** según Google, el sábado cierra a las 20:30, aunque otras webs dicen que abre hasta tarde.
- **Hermanos Vinagre:** su web da un horario algo distinto al de Google.

En la app real, los horarios deberían salir de una fuente que se mantenga al día, como la API de Google Places, para no tener que copiarlos a mano.

## Fun facts por bar (del plano oficial)

| Bar | Fun fact |
| --- | --- |
| Bodega de la Ardosa | Abrió en 1892 y conserva su barra de madera y su decoración antigua. Además del vermut de grifo, es famosa por su tortilla de patata y sus salazones. |
| Casa Camacho | Aquí nació el «yayo»: vermut, ginebra y gaseosa. Lleva tirando vermut de grifo desde los años 20. |
| Taberna Ángel Sierra | Conserva el vermut de grifo y su barra de roble, entre botellas antiguas. |
| Casa Alberto | Se fundó en 1827, es «Establecimiento Centenario» y está en pleno Barrio de las Letras, cargado de memoria literaria. |
| El Anciano Rey de los Vinos | Está frente al Palacio Real y sirve vinos y vermut de grifo en su barra de azulejo desde principios del siglo XX. |
| Bodegas Rosell | Abrió en 1920 junto a Atocha. Su fachada de azulejos es obra de Alfonso Romero, y todavía sirve vermut de barril. |
| Bodegas Casas | Es centenaria: abrió en 1923 y mantiene su barra de época. Tiene dos locales en la misma avenida. |
| La Violeta Vermut | Tiene más de 30 referencias de vermut: es el sitio para descubrir perfiles distintos. |
| Sifón | Es bodega y colmado a la vez: vermut bien servido con conservas de calidad, y una terraza muy codiciada. |
| Bodegas Alfaro | Es una bodega mítica de 1929, con vermut de grifo, barra de zinc y salazones. |
| Bodegas El Maño | Lleva desde 1927 junto a Conde Duque, sirviendo vermut con raciones clásicas como croquetas o bravas. |
| La Flaca Taberna | Los domingos tiene barra libre de vermut a precio fijo. |
| Taberna Alma Cheli | Presume de tener «uno de los mejores vermús» de la capital. |
| Hermanos Vinagre | Es un templo del aperitivo castizo: conservas, encurtidos y vermut bien servido. Tiene locales en Cardenal Cisneros, Narváez y Argumosa. |

## Pendiente

- Sacar las coordenadas de los 14 bares.
- Confirmar si Casa Camacho sigue abierto (Google avisa de que puede estar cerrado).
- Personalizar los criterios de la ficha.
- **Android y web, hallazgo de la prueba:** Android trata como «vibración al tocar» cualquier vibración de menos de 1 s que manda una web. Si ese ajuste está apagado, la ignora (la de 2 s sí vibra). Una web no puede indicar a Android de qué tipo es su vibración; una app nativa sí, por ejemplo como notificación o alarma, y así no depende de ese ajuste. Es otro motivo para hacer la versión final como app nativa.
- iOS: Safari no permite que una web haga vibrar el móvil. Para iPhone hace falta una app nativa (Core Haptics) o empaquetar la web con Capacitor o React Native.
- Probar en la calle cuál de los lenguajes A, B o C se entiende sin mirar la pantalla.
- Avisar de que se beba con moderación y de que, al cruzar, conviene mirar el semáforo y no confiar solo en la vibración.
