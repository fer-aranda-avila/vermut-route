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

El plano tiene 15 barras. No pude descargar el PDF desde el entorno de trabajo, así que estos 11 nombres salen de prensa y fuentes oficiales. **Hay que confirmar las direcciones con el plano.**

| # | Bar | Zona | Dirección |
| --- | --- | --- | --- |
| 1 | Bodega de la Ardosa | Malasaña | C/ Colón, 13 |
| 2 | Casa Camacho “Yayos” | Malasaña | C/ San Andrés, 4 |
| 3 | Casa Maravillas | Malasaña | por confirmar |
| 4 | Sifón Bodega Colmado | Malasaña | por confirmar |
| 5 | Bodegas El Maño | Conde Duque | C/ de la Palma, 64 |
| 6 | Taberna Ángel Sierra | Chueca | C/ Gravina, 11 |
| 7 | Hermanos Vinagre | por confirmar (tiene varios locales) | por confirmar |
| 8 | Casa Alberto | Las Letras | C/ Huertas, 18 |
| 9 | Bodegas Rosell | Atocha | C/ General Lacy, 11 |
| 10 | El Anciano Rey de los Vinos | Austrias | C/ Bailén, 19 |
| 11 | Bodegas Alfaro | Chamberí | por confirmar |
| 12–15 | por confirmar del plano | | |

## Fun facts (borrador)

- *Vermut* viene del alemán *Wermut*, que significa ajenjo.
- En la UE, al menos un 75 % del vermut tiene que ser vino, y su graduación va del 14,5 % al 22 %.
- La mayoría de los vermuts rojos se hacen con vino blanco: el color sale del caramelo y de las botánicas.
- El vermut moderno se atribuye a Antonio Benedetto Carpano (Turín, 1786).
- En Madrid se pide de grifo y con sifón; se acompaña de aceituna y naranja.
- «La hora del vermut» es el aperitivo de antes de comer, sobre todo el domingo.

## Pendiente

- Completar los 4 bares que faltan y confirmar las direcciones con el PDF.
- Personalizar los criterios de la ficha.
- **Android y web, hallazgo de la prueba:** Android trata como «vibración al tocar» cualquier vibración de menos de 1 s que manda una web. Si ese ajuste está apagado, la ignora (la de 2 s sí vibra). Una web no puede indicar a Android de qué tipo es su vibración; una app nativa sí, por ejemplo como notificación o alarma, y así no depende de ese ajuste. Es otro motivo para hacer la versión final como app nativa.
- iOS: Safari no permite que una web haga vibrar el móvil. Para iPhone hace falta una app nativa (Core Haptics) o empaquetar la web con Capacitor o React Native.
- Probar en la calle cuál de los lenguajes A, B o C se entiende sin mirar la pantalla.
- Avisar de que se beba con moderación y de que, al cruzar, conviene mirar el semáforo y no confiar solo en la vibración.
