:title Unreal Engine 5, manuelt minimalt prosjekt
:author finn
:tech []
:published 2022-10-30

:blurb

Unreal Engine 5 er et beist! I denne blogposten skal vi temme beistet litt ved å manuelt lage et prosjekt med filene som den trenger, kompilere dem og starte opp det nye prosjektet fra kommandolinja.

:body

## Motivasjon

En erfaring en får ved å lage software - er at det gir viktig forståelse å beherske hvordan en lager alle prosjekt-filer fra bunnen selv. Så kjøre alle de sentrale operasjonene dine fra kommandolinja. Dette i motsetning til wizards eller maler som populerer startprosjekt med innhold og filer som du ikke skjønner så mye av. 

La oss bootstrappe selv. Ingen trykking på Play-knapper i en IDE/Editor.

<img src="/images/blogg/2022-10-31-ue5-byggesystem/no-play.png" width="80px" style="margin: 16px 0;"/> 

## Make

Make er kjempelett. Alle kommandoene vi ser på i dag er i Make syntax - og de ligger i [en Makefile](https://github.com/finnjohnsen/blog-ue-fantastisk/blob/master/Makefile) på [github repoet](https://github.com/finnjohnsen/blog-ue-fantastisk).

## Hva skal make gjøre?

#### Hovedmålet er å kompilere 2 ting (dette er det viktigste å forstå i hele denne bloggposten)
1. En versjon av spillet ditt til .dll-fil, som Unreal Engine Editor (aka Beistet) bruker mens du utvikler spillet i editoren.
1. En standalone .exe for å kjøre spillet som en sluttbruker.

#### En ting må på plass før vi kan eksekvere standalone som sluttbruker

3. Cooking. Det er et artig ord for å kompilere assets. F.eks en jpeg texture til en native shader.

#### Så noen kjekke saker i Makefila

4. clean, rydder i prosjektet ditt - utenom source og assets. Halvfabrikater og kompilerte ting som vi har bygget.
5. Starte Editoren. Sånn at du slipper å dobbeltklikke på prosjektfila di.
6. Starte standalone .exe. Sånn at du slipper å dobbeltklikke på .exe -fila du har laga.

## Prosjektfiler

Vi kaller [dette prosjektet](https://github.com/finnjohnsen/blog-ue-fantastisk) "Fantastisk", og her er prosjektfilene pluss en liten dipp i c++ med to filer som bare logger oppstart/shutdown av spillet.

```
│   Fantastisk.uproject
│   Makefile
│
└───Source
    │   Fantastisk.Target.cs
    │   FantastiskEditor.Target.cs
    │   FantastiskMain.Build.cs
    │
    └───FantastiskMain
            FantastiskMain.cpp
            FantastiskMain.h
```

Det var jo ikke så mange filer synes jeg. Merk at vi lener oss på at et prosjekt uten assets fører til at UE velger et default kart som er innebygget i UE.

### TL;DR om prosjektfilene
1. [**Fantastisk.uproject**](https://github.com/finnjohnsen/blog-ue-fantastisk/blob/master/Fantastisk.uproject) er hovedfila hvor alt starter. Den kan du dobbeltklikke for å starte UE Editor (men ikke før du har bygget .dll-en). Og den refererer ihht konvensjon ([se "Name": "**FantastiskMain**"](https://github.com/finnjohnsen/blog-ue-fantastisk/blob/4728afc9cf75fec05a635cb928a806b2511550f6/Fantastisk.uproject#L8)) til **FantastiskMain.Build.cs**, **FantastiskEditor.Target.cs** og **Fantastisk.Target.cs**.

1. [**Source/FantastiskMain.Build.cs**](https://github.com/finnjohnsen/blog-ue-fantastisk/blob/master/Source/FantastiskMain.Build.cs) drar inn avhengigheter til moduler i UE som du skal bruke.

1. [**Source/FantastiskEditor.Target.cs**](https://github.com/finnjohnsen/blog-ue-fantastisk/blob/master/Source/FantastiskEditor.Target.cs) Target som er ansvarlig for å kompilere Editor .dll

1. [**Source/Fantastisk.Target.cs**](https://github.com/finnjohnsen/blog-ue-fantastisk/blob/master/Source/Fantastisk.Target.cs) Target som er ansvarlig for å kompilere standalone .exe


[C++ -filene](https://github.com/finnjohnsen/blog-ue-fantastisk/tree/master/Source/FantastiskMain) logger når du starter opp modulen (et spill == modul), og du skal se [de logglinjene](https://github.com/finnjohnsen/blog-ue-fantastisk/blob/master/Source/FantastiskMain/FantastiskMain.cpp#L7) i console når du starter.

##  Kommandoer

Ok, nå dykker vi litt ned i detaljer. 

~~~bash
UE_BYGG="C:\Program Files\Epic Games\UE_5.0\Engine\Build\BatchFiles\Build.bat"
UE_EDITOR="C:\Program Files\Epic Games\UE_5.0\Engine\Binaries\Win64\UnrealEditor.exe"
UE_EDITOR_CMD="C:\Program Files\Epic Games\UE_5.0\Engine\Binaries\Win64\UnrealEditor-Cmd.exe"
~~~

**Build.bat** kompilerer. UE har [sitt eget byggesystem](https://docs.unrealengine.com/5.0/en-US/using-the-unreal-engine-build-pipeline/).

I tillegg trenger vi å refere til editoren på to måter
1. **UE_EDITOR** som starter beistet (aka Unreal Engine Editor), som du faktisk utvikler spillet i. 
1. **UE_EDITOR_CMD** som kun *bruker editoren* i rein kommandolinje for å kompilere assets til native assets - for sitt respektive operativsystem. Vi bruker bare Win64 her da.

### La oss bygge .dll til UE Editor:


```make
UE_BYGG="C:\Program Files\Epic Games\UE_5.0\Engine\Build\BatchFiles\Build.bat"
U_PROJECT="C:\src\fantastisk\Fantastisk.uproject"

build-editor: 
	$(UE_BYGG) Fantastisk Win64 Development $(U_PROJECT) -waitmutex -NoHotReload
```

Kjør denne:
```
$ make build-editor
```

<details>
  <summary>Output</summary>

```bat
PS C:\src\fantastisk> make build-editor
"C:\Program Files\Epic Games\UE_5.0\Engine\Build\BatchFiles\Build.bat" FantastiskEditor Win64 Development "C:\src\fantastisk\Fantastisk.uproject" -waitmutex -NoHotReload
Using bundled DotNet SDK
Log file: C:\Users\finn\AppData\Local\UnrealBuildTool\Log.txt
Using 'git status' to determine working set for adaptive non-unity build (C:\src\fantastisk).
Creating makefile for FantastiskEditor (no existing makefile)
Parsing headers for FantastiskEditor
  Running UnrealHeaderTool "C:\src\fantastisk\Fantastisk.uproject" "C:\src\fantastisk\Intermediate\Build\Win64\FantastiskEditor\Development\FantastiskEditor.uhtmanifest" -LogCmds="loginit warning, logexit warning, logdatabase error" -Unattended -WarningsAsErrors -abslog="C:\Users\finn\AppData\Local\UnrealBuildTool\Log_UHT.txt" -installed
Reflection code generated for FantastiskEditor in 1,0929084 seconds
Building FantastiskEditor...
Using Visual Studio 2019 14.29.30146 toolchain (C:\Program Files (x86)\Microsoft Visual Studio\2019\Community\VC\Tools\MSVC\14.29.30133) and Windows 10.0.17763.0 SDK (C:\Program Files (x86)\Windows Kits\10).
Determining max actions to execute in parallel (16 physical cores, 24 logical cores)
  Executing up to 16 processes, one per physical core
Building 6 actions with 6 processes...
[1/6] Resource Default.rc2
[2/6] Compile SharedPCH.Engine.ShadowErrors.cpp
[3/6] Compile FantastiskMain.cpp
[4/6] Link UnrealEditor-FantastiskMain.lib
   Creating library C:\src\fantastisk\Intermediate\Build\Win64\UnrealEditor\Development\FantastiskMain\UnrealEditor-FantastiskMain.lib and object C:\src\fantastisk\Intermediate\Build\Win64\UnrealEditor\Development\FantastiskMain\UnrealEditor-FantastiskMain.exp
[5/6] Link UnrealEditor-FantastiskMain.dll
   Creating library C:\src\fantastisk\Intermediate\Build\Win64\UnrealEditor\Development\FantastiskMain\UnrealEditor-FantastiskMain.suppressed.lib and object C:\src\fantastisk\Intermediate\Build\Win64\UnrealEditor\Development\FantastiskMain\UnrealEditor-FantastiskMain.suppressed.exp
[6/6] WriteMetadata FantastiskEditor.target
Total time in Parallel executor: 23,96 seconds
Total execution time: 28,21 seconds
PS C:\src\fantastisk>
```
</details>

Nå fikk du masse filer, men essensen er denne fila: **Binaries/Win64/UnrealEditor-FantastiskMain.dll**



## Vi er klare for beistet

```
$ make editor
```

<img src="/images/blogg/2022-10-31-ue5-byggesystem/editor.png" width="100%" style="margin: 16px 0;"/> 

Artig? I UE Editor med forrige kommando kan du dra inn assets, herje rundt og lage spill. 


## Så, standalone .exe

Kjør disse kommandoene, så får vi i land denne standalone .exe.
```
$ make cook

$ make build-exe

$ make run-exe

```

<img src="/images/blogg/2022-10-31-ue5-byggesystem/exe.png" width="100%" /> 

PS. I stedet for *make run-exe* kan du dobbeltklikke på **Binaries/Win64/Fantastisk.exe**. Men da sender du ingen parametere og får fullskjerm.


## Logginga da?
Ja kikk litt i console loggen, så skal du se outputten fra c++ -koden vår.
<img src="/images/blogg/2022-10-31-ue5-byggesystem/log.png" width="100%" /> 
