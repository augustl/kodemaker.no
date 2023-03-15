:title Deklarativ validering av json i TypeScript
:author magnus
:tech [:programming :typescript :json]
:published 2023-03-15

:blurb

Du validerer vel json fra rest-kallene dine før du slipper dem løs i den nydelige typesikre domenemodellen din?
Kanskje du ikke gjør det, eller bare delvis, fordi det er skikkelig tungvint? 

I denne bloggposten ser vi på hvordan biblioteket [Zod](https://github.com/colinhacks/zod) 
kan hjelpe deg over kneika, slik at du virkelig kan begynne å stole på typene dine. 

:body
Du validerer vel json fra rest-kallene dine før du slipper dem løs i den nydelige typesikre domenemodellen din? 
Kanskje du ikke gjør det, eller bare delvis, fordi det er skikkelig tungvint?

I denne bloggposten ser vi på hvordan biblioteket [Zod](https://github.com/colinhacks/zod) kan hjelpe deg over kneika, slik at du virkelig kan begynne å stole på typene dine.


## Den skikkelig naive fremgangsmåten

Du har definert et sånt passe typesikkert domeneobjekt slik  
```typescript
type Card = {
    suit: string;
    rank: number;
}
```

Da kan det jo være fristende å slippe unna med noe alla:

```typescript
const aCard: Card = JSON.parse(`{"suit": "hearts", "rank": 1}`); 
```

Problem solved!

> Men hva skjer dersom du mot all formodning skulle få en json som ikke er helt i henhold hva du forventer?

```json 
{"suit": "harts", "rank": "ace"}
```
eller 
```json 
{"suit": "harts"}
```

Det vil jo åpenbart ikke gå bra. Men det som er enda verre er at det ikke vil feile når du "parser". Problemet
vil dukke opp et annet sted i programmet ditt, potensielt lenge etter at du hentet dataene.

Du kan jo selvfølgelig manuelt validere json objektet før du tilordner. Kanskje finner du en koselig bibliotek som hjelper deg
med å validere også. Det høres ut som mye jobb og føles ikke spesielt deklarativt. 
Alternativt kan du jo gå for å bruke json-schema, men det føles kanskje ut som overkill (og kodegenerering har jo noen ulemper det også).

> Hadde det ikke vært digg om du kunne definere validering og få generert typer fra en og samme kilde.
En ekstra bonus hadde vært om man kunne få strukturerte og lesbare feilmeldinger dersom kontrakten ikke ble overholdt ?

## En hjelpende hånd fra [Zod](https://github.com/colinhacks/zod)
Nei det er ikke en ond skurk fra Superman, men en liten kraftplugg som lar deg deklarativt 
definere validering og gir deg typer du kan bruke i appen din.

```typescript
import * as z from "zod";

// 1 - Skjema definisjon 
const CardSchema = z.object({
  suit: z.string(),
  rank: z.number(),
});

// 2 - Utled type basert på skjema
const Card = z.infer<typeof CardSchema>;
```

Nå har du et skjema du kan bruke for å parse og validere json og du har en type utledet fra skjema som har samme form som i forrige eksempel.

Hvordan parser jeg da ?

```typescript
const res = CardSchema.safeParse(json);
let aCard: Card
if (res.success) {
    aCard = res.data;
} else {
    // Dette er ikke måten å håndtere feil på...
    console.log(res.error);
}
```

Dersom du sender inn en gyldig json og Zod er enig, kan du trygt tilordne parseresultatet sitt data-attributt til typen `Card`.
Dersom validering feiler får du en et feilobjekt `error` i resultatet fra parsingen. Dette feilobjektet påpeker hva som er feil.


## En litt mer typesikker modell
`Card` typen vår er jo ikke så voldsomt typesikker. Kanskje vi kan forbedre den litt med å bruke noen enums ?

```typescript
// 1. Modellere enums

const SuitSchema = z.enum(["Hearts", "Spades", "Clubs", "Diamonds"]);
const Suit = z.infer<typeof SuitSchema>;

const RankSchema = z.enum(["Ace", "2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen", "King"]);
const Rank = z.infer<typeof RankSchema>;

// 2. Forbedre CardSchema til å sjekke at kun gyldige enum verdier er brukt 
const CardSchema = z.object({
    suit: SuitSchema,
    rank: RankSchema,
});

// 3. Bonus: Modellere en kortstokk
const DeckSchema = z.array(CardSchema);
```

Når du nå parser følgende json

```json
[
  {
    "suit": "Hearts",
    "rank": "Ace"
  },
  {
    "suit": "Clubs",
    "rank": "Jack"
  }
]
```
så vil zod sjekke at du har en array med gyldige kort som igjen har kun gyldige verdier for `suit` og `rank`.

Du får også full code completion for objektet dine:
```typescript
cards[0].sui // autocompletes to cards[0].suit
```

Du kan også bruke enum (med autocomplete) i koden din som følger:
```typescript
SuitSchema.enum.Clubs
```

## Veien videre
Vi har såvidt skrapt i overflaten for hva du kan gjøre med Zod. Du kan gå mye lenger
i hvor typesikker du ønsker modellen din. For å lære mer om Zod anbefaler jeg at du sjekker ut [dokumentasjonen](https://zod.dev/)

Forhåpentligvis har jeg overbevist deg om at det er lurt å validere dataene du får fra utenomverden (grensekontroll).
Det trenger ikke å være så vanskelig heller, dersom du har et godt bibliotek til å hjelpe deg.




