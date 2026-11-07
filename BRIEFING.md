# Ruta del Vermut háptica · Briefing

Una ruta a pie por vermuterías de Madrid **sin mapa**: el móvil va en el bolsillo y te guía solo con vibraciones. En cada parada te da un fun fact sobre el vermut y una ficha para valorarlo. Al final, un ranking.

Prototipo (canvas de diseño): https://claude.ai/artifact/VPpvtQ1zctbNzwm24ETsSD

Probador háptico para Android: [`docs/index.html`](docs/index.html), que GitHub Pages publica en https://fer-aranda-avila.github.io/vermut-route/ cuando se activa Pages con la carpeta `/docs`.

## Decisiones tomadas

| Tema | Decisión |
| --- | --- |
| Dispositivo | Móvil en el bolsillo. La pantalla solo se usa para empezar, al llegar y para la ficha. |
| Lenguaje háptico | Se prueban tres (A, B y C, abajo) antes de elegir uno. |
| Ruta | La eligen los usuarios: marcan de 3 a 5 bares y la app los ordena. Opcional: *modo sorpresa*, que oculta el nombre del bar hasta llegar. |
| Ficha | Una ficha corta tras cada bar y un ranking al final de la ruta. |

## Flujo

1. Bienvenida
2. Elegir bares (filtro por barrio)
3. Ruta ordenada, modo sorpresa y elección del lenguaje háptico
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

## Bares del plano (Academia Madrileña de Gastronomía / esMADRID)

Los 15 bares, con la numeración, las direcciones, los barrios y el metro tal y como vienen en el plano oficial.

| # | Bar | Dirección | Barrio (según el plano) | Metro |
| --- | --- | --- | --- | --- |
| 1 | Bodega de la Ardosa | C/ Colón, 13 | Recoletos | Tribunal · Santo Domingo |
| 2 | Casa Camacho “Yayos” | C/ San Andrés, 4 | Universidad | Tribunal |
| 3 | Taberna Ángel Sierra | C/ Gravina, 11 | Chueca | Tribunal · Santo Domingo |
| 4 | Casa Alberto | C/ de las Huertas, 18 | Las Letras | Antón Martín · Sol |
| 5 | El Anciano Rey de los Vinos | C/ de Bailén, 19 | Palacio | Ópera · La Latina |
| 6 | Bodegas Rosell | C/ del General Lacy, 14 | Delicias | Delicias · Palos de la Frontera |
| 7 | Bodegas Casas | Av. Ciudad de Barcelona, 23 (y otro local en el 57) | Pacífico | Pacífico |
| 8 | La Violeta Vermut | C/ de Vallehermoso, 62 | Vallehermoso | Canal · Quevedo |
| 9 | Sifón Bodega Colmado | Plaza del Rey, 4 (en el mapa pone 9) | Chueca | Recoletos |
| 10 | Bodegas Alfaro | C/ Ave María, 10 | Lavapiés | Lavapiés |
| 11 | Bodegas El Maño | C/ de la Palma, 64 | Universidad | Tribunal · Noviciado |
| 12 | Casa Maravillas | C/ de Manuela Malasaña, 13 | Malasaña | Bilbao · San Bernardo |
| 13 | La Flaca Taberna | C/ de Serrano, 43 | Salamanca | Serrano · Retiro |
| 14 | Taberna Alma Cheli | C/ de Santa Engracia, 103 | Trafalgar | Ríos Rosas · Alonso Martínez |
| 15 | Hermanos Vinagre | C/ Argumosa, 12 (también Cardenal Cisneros y Narváez) | Lavapiés | Lavapiés · Antón Martín |

Pendiente de aclarar con el plano o en la calle:
- **Sifón:** la ficha dice Plaza del Rey, 4 y la etiqueta del mapa, Plaza del Rey, 9.
- **Bodegas Casas** y **Hermanos Vinagre** tienen más de un local: hay que elegir uno para la ruta.
- **La Ardosa:** el plano la pone en el barrio de Recoletos, aunque en la descripción dice Malasaña.

El plano también trae datos de cada bar que sirven como fun facts propios de esa parada: el año de fundación de cada bar, que el «yayo» (vermut, ginebra y gaseosa) se inventó en Casa Camacho, que La Violeta tiene más de 30 vermuts o que la fachada de azulejos de Bodegas Rosell es de Alfonso Romero.

## Fun facts (borrador)

- *Vermut* viene del alemán *Wermut*, que significa ajenjo.
- En la UE, al menos un 75 % del vermut tiene que ser vino, y su graduación va del 14,5 % al 22 %.
- La mayoría de los vermuts rojos se hacen con vino blanco: el color sale del caramelo y de las botánicas.
- El vermut moderno se atribuye a Antonio Benedetto Carpano (Turín, 1786).
- En Madrid se pide de grifo y con sifón; se acompaña de aceituna y naranja.
- «La hora del vermut» es el aperitivo de antes de comer, sobre todo el domingo.

## Pendiente

- Sacar las coordenadas de los 15 bares a partir de las direcciones y resolver las dudas de Sifón, Bodegas Casas y Hermanos Vinagre.
- Personalizar los criterios de la ficha.
- **Android y web, hallazgo de la prueba:** Android trata como «vibración al tocar» cualquier vibración de menos de 1 s que manda una web. Si ese ajuste está apagado, la ignora (la de 2 s sí vibra). Una web no puede indicar a Android de qué tipo es su vibración; una app nativa sí, por ejemplo como notificación o alarma, y así no depende de ese ajuste. Es otro motivo para hacer la versión final como app nativa.
- iOS: Safari no permite que una web haga vibrar el móvil. Para iPhone hace falta una app nativa (Core Haptics) o empaquetar la web con Capacitor o React Native.
- Probar en la calle cuál de los lenguajes A, B o C se entiende sin mirar la pantalla.
- Avisar de que se beba con moderación y de que, al cruzar, conviene mirar el semáforo y no confiar solo en la vibración.
