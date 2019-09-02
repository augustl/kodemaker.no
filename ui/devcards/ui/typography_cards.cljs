(ns ui.typography-cards
  (:require [devcards.core :refer-macros [defcard]]
            [sablono.core :refer [html]]
            [ui.typography :as typography]))

(defcard h1
  (html (typography/h1 {} "Et unikt team av seniortviklere")))

(defcard h2-acting-as-h1
  (html (typography/h1 {:element :h2} "Et unikt team av seniortviklere")))

(defcard h2
  (html (typography/h2 {} "Artikler og innsikt")))

(defcard h1-acting-as-h2
  (html (typography/h2 {:element :h1} "Artikler og innsikt")))

(defcard h3
  (html (typography/h3 {} "Vi har levert")))

(defcard h1-acting-as-h3
  (html (typography/h3 {:element :h1} "Vi har levert")))

(defcard h4
  (html (typography/h4 {} "Se hvem vi er på laget")))

(defcard h1-acting-as-h4
  (html (typography/h4 {:element :h1} "Se hvem vi er på laget")))

(defcard h5
  (html (typography/h5 {} "Kontakt")))

(defcard h1-acting-as-h5
  (html (typography/h5 {:element :h1} "Kontakt")))

(defcard paragraph
  (html [:p {} "To Kodemakere står bak det teknologiske ved Oche sitt automatiserte konsept for dartspill. Bildeanalyse, spillutvikling, grensesnitt og oppsett/overvåkning av maskiner og utstyr."]))

(defcard blockquote
  (html (typography/blockquote
         {}
         "Kodemaker tok en idé til ferdig løsning på kort tid, og de har vært en viktig ekstern bidragsyter i utviklingen av vårt konsept Oche. De har jobbet godt sammen med flere andre aktører i et hektisk prosjekt.

De er flinke, sier hva de mener og lager det vi ønsker. Softwaren de har laget
har fungert knirkefritt siden åpningen. Vi har et veldig godt inntrykk av hele
Kodemaker, og de fremstår som en dyktig, jovial og humørfylt gjeng.")))
