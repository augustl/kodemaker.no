:title Public keys og private keys
:author august
:tech [:ssl :rsa :ecc]
:published 2019-08-28

:blurb

Som utvikler trenger du å vite hva public keys og private keys er, og hva du kan bruke de til. I denne artikkelen lærer du alt du trenger å vite - uten noe matematikk!

:body


Jeg var utvikler i mange år uten å helt forstå hva public keys og private keys var. Men det tar ikke veldig lang tid som utvikler før du treffer borti dem på et eller annet vis. Kanskje allerede i den første måneden som utvikler får du spørsmål om å sende noen din SSH public key. Og du kjører villig `ssh-keygen` slik Google anbefaler, uten å helt forstå hva som var bedre enn å bare skrive inn et passord.

Men fortvil ikke. Du vet allerede hvordan public keys og private keys fungerer! Helt sant! Du vet jo hvordan en hengelås fungerer. Det betyr at du forstår asymmetrisk kryptografi. 

Alt du trenger for å forstå det store bildet, er et par søte animasjoner.


## Å trygt dele en hemmelighet - uten og møtes først

Hvordan kan man helt trygt dele en hemmelighet, uten å utveksle nøkler eller hengelåser eller andre hemmeligheter først?

Dette er kanskje ikke så intuitivt. Det var ihvertfall ikke det for meg. Men det er faktisk helt mulig!

<video muted playsinline autoplay id="back_and_forth_video" controls width="640" style="max-width: 100%">
  <source src="/videos/blogg/back-and-forth.mp4" type="video/mp4">
</video>

<ol id="back_and_forth_steps">
<li data-start="0.0">Alice putter hemmeligheten i en koffert</li>
<li data-start="3.5">Alice putter sin lås på kofferten</li>
<li data-start="6.2">Alice sender kofferten til Bob</li>
<li data-start="8.0">Bob putter sin lås på kofferten</li>
<li data-start="10.5">Bob sender kofferten tilbake til Alice</li>
<li data-start="12.5">Alice låser opp sin lås fra kofferten</li>
<li data-start="15.0">Alice sender kofferten tilbake til Bob</li>
<li data-start="16.5">Bob låser opp sin hengelås</li>
</ol>

## Å trygt dele en hemmelighet - med public og private keys

Det blir en del frem og tilbake, da. Og så var det helt uten public og private keys. Det finnes heldigvis en smartere måte. En hengelås har nemlig to tilstander: låst, og ulåst. Og det kan vi benytte til vår fordel.

Den ulåste hengelåsen er din public key. Det er ikke noe hemmelig med en hengelås. Du kan lukte på den, snu rundt på den, høre på den, veie den, helt uten å avdekke noen hemmeligheter. Husk, hengelåsen vår er en _matematisk hengelås_. En ekte hengelås kan man slå med en hammer til man får se hva som er inni. En matematisk hengelås er annerledes. Alle verdens smarte mennesker prøver så godt de kan, men likevel er det ingen kjente måter for å se inni hengelåsen. _Den kan ikke reverse engineeres_.

Derfor kan du gi fra deg en ulåst hengelås, og det er helt trygt og sikkert. Da blir prosessen vår mye smartere.

<video muted playsinline autoplay id="real_deal_video" controls width="640" style="max-width: 100%">
  <source src="/videos/blogg/real-deal.mp4" type="video/mp4">
</video>

<ol id="real_deal_steps">
<li data-start="0.0">Bob sender sin åpne hengelås til Alice</li>
<li data-start="5.5">Alice putter hemmeligheten i en koffert</li>
<li data-start="8.2">Alice låser kofferten med den åpne hengelåsen (public key)</li>
<li data-start="9.8">Alice sender kofferten til Bob</li>
<li data-start="12.0">Bob låser opp kofferten med sin nøkkel (private key)</li>
</ol>

Gratulerer, du har nå forstått hvordan public og private keys fungerer!

## Og nå, mitt neste triks: software!

For å demonstrere software-biten av dette, skal jeg bruke Ruby (hei, Knut!). OpenSSL har et kommandolinje-grensesnitt, men det er litt knotete og rotete. I Ruby er det objekt-orientert og er litt lettere å grokke.

La oss gjøre akkurat det samme i software som vi gjorde med koffert-animasjonen ovenfor.

````ruby
require "openssl"

data = "Some secret data is here."

# Bob filer seg en nøkkel. Den er tilfeldig generert.
# (Bob kan også bruke en eksisterende nøkkel, naturligvis)
priv_key = OpenSSL::PKey::RSA.new(2048)

# Bob bruker nøkkelen til å lage en ny, åpen hengelås
pub_key = priv_key.public_key

# Bob sender en åpen hengelås til Alice
send_homing_pigeon(:alice, pub_key)

# Alice putter hemmeligheten i en koffert, og låser kofferten med hengelåsen
encrypted = pub_key.public_encrypt(data)

# Alice sender kofferten til Bob
send_homing_pigeon(:bob, encrypted)

# Bob låser opp hengelåsen
decrypted = priv_key.private_decrypt(encrypted)
````

Voila!

## Software-nøkler kan brukes begge veier!

Analogier er glimrende til å forklare ting for første gang. En zebra er som en hest, bare at den har striper. Men en hest som er malt svart og hvit er ikke en zebra. La oss legge analogien til side, og forholde oss til public keys og private keys.

I eksempelet over, krypterer vi med public keyen (åpen hengelås) og dekrypterer med private keyen (nøkkelen). Men man kan også kryptere med private keyen, og dekryptere med public keyen.

````ruby
require "openssl"

data = "Some secret data is here."

priv_key = OpenSSL::PKey::RSA.new(2048)
pub_key = priv_key.public_key

encrypted = priv_key.private_encrypt(data)
decrypted = pub_key.public_decrypt(encrypted)
````

## Og de kan brukes til å verifisere identitet

Jeg sender deg min public key, og en kryptert melding.

Du dekrypterer meldingen, og ser at det jeg skriver til deg er:

```text
// Melding A (fake)
��9E;jd:D�o��X}�!��4�>9o�bۺ

```

Du kjenner jo meg. Jeg sender _aldri_ sånne tøyse-meldinger.

Heldigvis fikk du enda en melding. Du dekrypterer den, og får:

```text
// Melding B (legit)
Du er flink som kan crypto!

```

Du klarte å dekryptere meldingen. Med min public key.

Hva betyr det?

Du har en **kryptografisk garanti på at det var jeg som sendte meldingen!** Den første meldingen lot seg tilsynelatende ikke dekryptere, siden du bare fikk tøysedata. Den andre meldingen så helt fin ut. Og det er kun jeg som er i stand til å generere krypterte meldinger som kan dekrypteres med min public key.

## Så hva skjer når du logger inn med SSH?

Hvorfor er SSH bedre med public keys enn passord?

Det er fordi ingen ukrypterte hemmeligheter blir sendt over kabelen.

Når du logger på med SSH, ligger allerede din public key inne på serveren (den derre `id_rsa.pub`). Serveren genererer et tilfeldig tall, og sender det til deg. Du krypterer det tallet med din private key (den fila som typisk ligger i `~/.ssh/id_rsa`), og sender det krypterte tallet til serveren. Serveren dekrypterer med public keyen. Fikk den det samme tallet tilbake som den sendte fra seg, vet serveren at du som logger på faktisk besitter private keyen. Og ingen passord ble utvekslet.

## Et par ord om matematikken

Det er ganske kult at jeg ikke har sagt et kvekk om matematikk. Likevel har du lært hvordan private og public keys fungerer! Men nå er det på tide å kvekke litt.

Jeg vil bare vise deg litt om prinsippene bak, slik at du stoler på at det faktisk eksisterer en matematisk hengelås.

<small>Det har naturligvis ingenting å gjøre med min mangel på faktisk dybdekunnskap.</small>

For å kunne lage en matematisk hengelås, trenger man en _enveisfunksjon_.

Én slik enveisfunksjon er RSA. Denne baserer seg på faktorisering. Fort, finn hvilke to tall du må multiplisere for å få 61657329! Ikke så lett? Svaret er 5001 og 12329. Men ifølge matematikerne er det ingen smart måte å komme frem til det på, utover brute force. Hvis de to tallene dine er skikkelig store, har du i praksis en matematisk hengelås som ikke kan brytes opp. Hvis de to tallene i tillegg er primtall, kan smart matematikk gjøre crypto-greier. Private keyen din er to store primtall (because maths), og public keyen er resultatet av å multiplisere disse to primtallene.

Den andre er elliptic curve. Siden dette baserer seg på, vel, elliptiske kurver, så er den litt vanskelig å forklare med bare tekst. [Numberphile har en glimrende video](https://www.youtube.com/watch?v=NF1pwjL9-DE). Kort fortalt, handler det om at du finner et punkt på kurven, og flytter deg rundt N ganger på kurven ved å legge punktet til seg selv. Private keyen din er punktet du startet på, og hvor stort N er, public keyen din er produktet av disse.

Det finnes flere. Men dette er de to mest brukte.

## Et par ord om kvante-datamaskiner

Det som er litt morsomt (this is fine, everything is fine) er at kvantemaskiner faktoriserer på et øyeblikk.

Med andre ord, kvantemaskiner brekker enveisfunksjonen til faktorisering.

De gode nyhetene, er at det meste en kvantemaskin klarer i dag, er å få inn 15, og umiddelbart spytte ut 3 og 5. Ikke bare fordi de er veldig kjappe eller noe, den algoritmiske kompleksiteten er faktisk konstant.

Det er ikke bare bare å skalere kvantemaskiner. Så med litt flaks, lever vi lykkelig i alle våre dager.


<script type="text/javascript">
(function (GLOBAL) {
    function augustPublicPrivateKeysGoGoGo(videoElId, stepsElId) {
        var videoEl = document.getElementById(videoElId);
        videoEl.loop = true;
        videoEl.preload = "auto";
        videoEl.removeAttribute("controls");
        
        var stepsEl = document.getElementById(stepsElId);
        var stepElements = stepsEl.querySelectorAll("li");
        var steps = [];
        for (var i = 0; i < stepElements.length; i++) {
          (function (i) {
            var stepElement = stepElements[i];
            var timecode = parseFloat(stepElement.getAttribute("data-start"));
            steps.push({
              timecode: timecode,
              el: stepElement,
              idx: i
            });
            
            stepElement.addEventListener("click", function () {
              videoEl.currentTime = timecode;
            })
          }(i))
        }
                
        // var currentTimerEl = document.createElement("div");
        // videoEl.parentNode.insertBefore(currentTimerEl, videoEl.nextSibling);
        
        var prevStepIdx = null;
        function updateCurrentStepInDOM() {
            var currentTime = videoEl.currentTime;
            // currentTimerEl.textContent = currentTime;
            
            var currentStep = steps.filter(function (step) { return step.timecode > currentTime; })[0]
            var currStepIdx;
            if (currentStep) {
              currStepIdx = currentStep.idx - 1;
            } else {
               currStepIdx = steps.length - 1;
            }
            
            if (prevStepIdx !== currStepIdx) {
              steps[currStepIdx].el.classList.add("highlighted-video-element");
              
              if (prevStepIdx !== null) {
                steps[prevStepIdx].el.classList.remove("highlighted-video-element");
              }
            }
            
            prevStepIdx = currStepIdx;
        }
        
        videoEl.addEventListener("timeupdate", function (e) {
          updateCurrentStepInDOM()
        })
        
        var timeoutId;
        function tickAnimationLoop() {
          clearTimeout(timeoutId);
          
          timeoutId = setTimeout(function () {
            updateCurrentStepInDOM();
            tickAnimationLoop();
          }, 100)
        }
        
        tickAnimationLoop();
    }

    GLOBAL.augustPublicPrivateKeysGoGoGo = augustPublicPrivateKeysGoGoGo
}(window))
</script>

<script type="text/javascript">
augustPublicPrivateKeysGoGoGo("back_and_forth_video", "back_and_forth_steps")
augustPublicPrivateKeysGoGoGo("real_deal_video", "real_deal_steps")
</script>

<style type="text/css">
.highlighted-video-element {
  background-color: rgb(255, 251, 214);
}
</style>
