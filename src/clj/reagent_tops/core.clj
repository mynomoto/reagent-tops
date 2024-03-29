(ns reagent-tops.core
  (:require [ring.util.response :refer [file-response]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.edn :refer [wrap-edn-params]]
            [compojure.core :refer [defroutes GET PUT]]
            [compojure.route :as route]
            [compojure.handler :as handler]))

(def prod false)

(defn index []
  (if prod
    (file-response "public/html/index-prod.html" {:root "resources"})
    (file-response "public/html/index.html" {:root "resources"})))

(defn generate-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body (pr-str data)})

(def words
  "http://www.poslarchive.com/math/scrabble/lists/common-6.html"
  ["baaing" "baalim" "baases" "babble" "babels" "babied"
   "babier" "babies" "babkas" "babool" "baboon" "baboos" "babuls" "baccae" "bached" "baches"
   "backed" "backer" "backup" "bacons" "bacula" "badass" "badder" "baddie" "badged" "badger"
   "badges" "badman" "badmen" "baffed" "baffle" "bagass" "bagels" "bagful" "bagged" "bagger"
   "baggie" "bagman" "bagmen" "bagnio" "baguet" "bagwig" "bailed" "bailee" "bailer" "bailey"
   "bailie" "bailor" "bairns" "baited" "baiter" "baizas" "baizes" "bakers" "bakery" "baking"
   "balata" "balboa" "balded" "balder" "baldly" "baleen" "balers" "baling" "balked" "balker"
   "ballad" "balled" "baller" "ballet" "ballon" "ballot" "ballsy" "balsam" "balsas" "bamboo"
   "bammed" "banana" "bancos" "bandas" "banded" "bander" "bandit" "bandog" "banged" "banger"
   "bangle" "banian" "baning" "banish" "banjax" "banjos" "banked" "banker" "bankit" "banned"
   "banner" "bannet" "bantam" "banter" "banyan" "banzai" "baobab" "barbal" "barbed" "barbel"
   "barber" "barbes" "barbet" "barbie" "barbut" "barcas" "barded" "bardes" "bardic" "barege"
   "barely" "barest" "barfed" "barfly" "barged" "bargee" "barges" "barhop" "baring" "barite"
   "barium" "barked" "barker" "barley" "barlow" "barman" "barmen" "barmie" "barned" "barney"
   "barong" "barons" "barony" "barque" "barred" "barrel" "barren" "barres" "barret" "barrio"
   "barrow" "barter" "baryes" "baryon" "baryta" "baryte" "basalt" "basely" "basest" "bashaw"
   "bashed" "basher" "bashes" "basics" "basify" "basils" "basing" "basins" "basion" "basked"
   "basket" "basque" "basses" "basset" "bassly" "bassos" "basted" "baster" "bastes" "batboy"
   "bateau" "bathed" "bather" "bathes" "bathos" "batiks" "bating" "batman" "batmen" "batons"
   "batted" "batten" "batter" "battik" "battle" "battue" "baubee" "bauble" "baulks" "baulky"
   "bawbee" "bawdry" "bawled" "bawler" "bawtie" "bayamo" "bayard" "baying" "bayman" "baymen"
   "bayous" "bazaar" "bazars" "bazoos" "beachy" "beacon" "beaded" "beader" "beadle" "beagle"
   "beaked" "beaker" "beamed" "beaned" "beanie" "beanos" "beards" "bearer" "beasts" "beaten"
   "beater" "beauts" "beauty" "beaver" "bebops" "becalm" "became" "becaps" "becked" "becket"
   "beckon" "beclog" "become" "bedamn" "bedaub" "bedbug" "bedded" "bedder" "bedeck" "bedell"
   "bedels" "bedews" "bedims" "bedlam" "bedpan" "bedrid" "bedrug" "bedsit" "beduin" "bedumb"
   "beebee" "beechy" "beefed" "beeped" "beeper" "beetle" "beeves" "beezer" "befall" "befell"
   "befits" "beflag" "beflea" "befogs" "befool" "before" "befoul" "befret" "begall" "begaze"
   "begets" "beggar" "begged" "begins" "begird" "begirt" "beglad" "begone" "begrim" "begulf"
   "begums" "behalf" "behave" "behead" "beheld" "behest" "behind" "behold" "behoof" "behove"
   "behowl" "beiges" "beigne" "beings" "bekiss" "beknot" "belady" "belaud" "belays" "beldam"
   "beleap" "belfry" "belgas" "belied" "belief" "belier" "belies" "belike" "belive" "belled"
   "belles" "bellow" "belong" "belons" "belows" "belted" "belter" "beluga" "bemata" "bemean"
   "bemire" "bemist" "bemixt" "bemoan" "bemock" "bemuse" "bename" "benday" "bended" "bendee"
   "bender" "bendys" "benign" "bennes" "bennet" "bennis" "bentos" "benumb" "benzal" "benzin"
   "benzol" "benzyl" "berake" "berate" "bereft" "berets" "berime" "berlin" "bermed" "bermes"
   "bertha" "berths" "beryls" "beseem" "besets" "beside" "besmut" "besnow" "besoms" "besots"
   "bested" "bestir" "bestow" "bestud" "betake" "betels" "bethel" "betide" "betime" "betise"
   "betons" "betony" "betook" "betray" "bettas" "betted" "better" "bettor" "bevels" "bevies"
   "bevors" "bewail" "beware" "beweep" "bewept" "bewigs" "beworm" "bewrap" "bewray" "beylic"
   "beylik" "beyond" "bezant" "bezazz" "bezels" "bezils" "bezoar" "bhakta" "bhakti" "bhangs"
   "bharal" "bhoots" "bialis" "bialys" "biased" "biases" "biaxal" "bibbed" "bibber" "bibles"
   "bicarb" "biceps" "bicker" "bicorn" "bicron" "bidden" "bidder" "biders" "bidets" "biding"
   "bields" "biface" "biffed" "biffin" "biflex" "bifold" "biform" "bigamy" "bigeye" "bigger"
   "biggie" "biggin" "bights" "bigots" "bigwig" "bijous" "bijoux" "bikers" "bikies" "biking"
   "bikini" "bilboa" "bilbos" "bilged" "bilges" "bilked" "bilker" "billed" "biller" "billet"
   "billie" "billon" "billow" "bimahs" "bimbos" "binary" "binate" "binder" "bindis" "bindle"
   "biners" "binged" "binger" "binges" "bingos" "binits" "binned" "binocs" "biogas" "biogen"
   "biomes" "bionic" "bionts" "biopic" "biopsy" "biotas" "biotic" "biotin" "bipack" "bipeds"
   "bipods" "birded" "birder" "birdie" "bireme" "birkie" "birled" "birler" "birles" "birred"
   "birses" "births" "bisect" "bishop" "bisons" "bisque" "bister" "bistre" "bistro" "bitchy"
   "biters" "biting" "bitmap" "bitted" "bitten" "bitter" "bizone" "bizzes" "blabby" "blacks"
   "bladed" "blader" "blades" "blaffs" "blains" "blamed" "blamer" "blames" "blanch" "blanks"
   "blared" "blares" "blasts" "blasty" "blawed" "blazed" "blazer" "blazes" "blazon" "bleach"
   "bleaks" "blears" "bleary" "bleats" "blebby" "bleeds" "bleeps" "blench" "blende" "blends"
   "blenny" "blight" "blimey" "blimps" "blinds" "blinis" "blinks" "blintz" "blites" "blithe"
   "bloats" "blocks" "blocky" "blokes" "blonde" "blonds" "bloods" "bloody" "blooey" "blooie"
   "blooms" "bloomy" "bloops" "blotch" "blotto" "blotty" "blouse" "blousy" "blowby" "blowed"
   "blower" "blowsy" "blowup" "blowzy" "bludge" "bluely" "bluest" "bluesy" "bluets" "blueys"
   "bluffs" "bluing" "bluish" "blumed" "blumes" "blunge" "blunts" "blurbs" "blurry" "blurts"
   "blypes" "boards" "boarts" "boasts" "boated" "boatel" "boater" "bobbed" "bobber" "bobbin"
   "bobble" "bobcat" "bocces" "boccia" "boccie" "boccis" "boches" "bodega" "bodice" "bodied"
   "bodies" "bodily" "boding" "bodkin" "boffed" "boffin" "boffos" "bogans" "bogart" "bogeys"
   "bogged" "boggle" "bogies" "bogles" "boheas" "bohunk" "boiled" "boiler" "boings" "boinks"
   "boites" "bolder" "boldly" "bolero" "bolete" "boleti" "bolide" "bolled" "bollix" "bollox"
   "bolshy" "bolson" "bolted" "bolter" "bombax" "bombed" "bomber" "bombes" "bombyx" "bonaci"
   "bonbon" "bonded" "bonder" "bonduc" "boners" "bonged" "bongos" "bonier" "boning" "bonita"
   "bonito" "bonked" "bonnes" "bonnet" "bonnie" "bonobo" "bonsai" "bonzer" "bonzes" "boobed"
   "boobie" "booboo" "boocoo" "boodle" "booger" "boogey" "boogie" "boohoo" "booing" "boojum"
   "booked" "booker" "bookie" "bookoo" "boomed" "boomer" "boosts" "booted" "bootee" "booths"
   "bootie" "boozed" "boozer" "boozes" "bopeep" "bopped" "bopper" "borage" "borals" "borane"
   "borate" "bordel" "border" "boreal" "boreas" "boreen" "borers" "boride" "boring" "borked"
   "borons" "borrow" "borsch" "borsht" "borzoi" "boshes" "bosker" "bosket" "bosoms" "bosomy"
   "bosons" "bosque" "bossed" "bosses" "boston" "bosuns" "botany" "botchy" "botels" "botfly"
   "bother" "bottle" "bottom" "boubou" "boucle" "boudin" "bouffe" "boughs" "bought" "bougie"
   "boules" "boulle" "bounce" "bouncy" "bounds" "bounty" "bourgs" "bourne" "bourns" "bourse"
   "boused" "bouses" "bouton" "bovids" "bovine" "bowels" "bowers" "bowery" "bowfin" "bowing"
   "bowled" "bowleg" "bowler" "bowman" "bowmen" "bowpot" "bowsed" "bowses" "bowwow" "bowyer"
   "boxcar" "boxers" "boxful" "boxier" "boxily" "boxing" "boyard" "boyars" "boyish" "boylas"
   "braced" "bracer" "braces" "brachs" "bracts" "braggy" "brahma" "braids" "brails" "brains"
   "brainy" "braise" "braize" "braked" "brakes" "branch" "brands" "brandy" "branks" "branny"
   "brants" "brashy" "brasil" "brassy" "bratty" "bravas" "braved" "braver" "braves" "bravos"
   "brawer" "brawls" "brawly" "brawns" "brawny" "brayed" "brayer" "brazas" "brazed" "brazen"
   "brazer" "brazes" "brazil" "breach" "breads" "bready" "breaks" "breams" "breast" "breath"
   "bredes" "breech" "breeds" "breeks" "breeze" "breezy" "bregma" "brents" "breves" "brevet"
   "brewed" "brewer" "brewis" "briard" "briars" "briary" "bribed" "bribee" "briber" "bribes"
   "bricks" "bricky" "bridal" "brides" "bridge" "bridle" "briefs" "briers" "briery" "bright"
   "brillo" "brills" "brined" "briner" "brines" "brings" "brinks" "briony" "brises" "brisks"
   "briths" "britts" "broach" "broads" "broche" "brocks" "brogan" "brogue" "broils" "broken"
   "broker" "brolly" "bromal" "bromes" "bromic" "bromid" "bromin" "bromos" "bronco" "broncs"
   "bronze" "bronzy" "brooch" "broods" "broody" "brooks" "brooms" "broomy" "broses" "broths"
   "brothy" "browed" "browns" "browny" "browse" "brucin" "brughs" "bruins" "bruise" "bruits"
   "brulot" "brumal" "brumby" "brumes" "brunch" "brunet" "brunts" "brushy" "brutal" "bruted"
   "brutes" "bruxed" "bruxes" "bryony" "bubale" "bubals" "bubbas" "bubble" "bubbly" "bubkes"
   "buboed" "buboes" "buccal" "bucked" "bucker" "bucket" "buckle" "buckos" "buckra" "budded"
   "budder" "buddha" "buddle" "budged" "budger" "budges" "budget" "budgie" "buffed" "buffer"
   "buffet" "buffos" "bugeye" "bugged" "bugger" "bugled" "bugler" "bugles" "bugout" "bugsha"
   "builds" "bulbar" "bulbed" "bulbel" "bulbil" "bulbul" "bulged" "bulger" "bulges" "bulgur"
   "bulked" "bullae" "bulled" "bullet" "bumble" "bumkin" "bummed" "bummer" "bumped" "bumper"
   "bumphs" "bunchy" "buncos" "bundle" "bundts" "bunged" "bungee" "bungle" "bunion" "bunked"
   "bunker" "bunkos" "bunkum" "bunted" "bunter" "bunyas" "buoyed" "bupkes" "bupkus" "buppie"
   "buqsha" "burans" "burble" "burbly" "burbot" "burden" "burdie" "bureau" "burets" "burgee"
   "burger" "burghs" "burgle" "burgoo" "burial" "buried" "burier" "buries" "burins" "burkas"
   "burked" "burker" "burkes" "burlap" "burled" "burler" "burley" "burned" "burner" "burnet"
   "burnie" "burped" "burqas" "burred" "burrer" "burros" "burrow" "bursae" "bursal" "bursar"
   "bursas" "burses" "bursts" "burton" "busbar" "busboy" "bushed" "bushel" "busher" "bushes"
   "bushwa" "busied" "busier" "busies" "busily" "busing" "busked" "busker" "buskin" "busman"
   "busmen" "bussed" "busses" "busted" "buster" "bustic" "bustle" "butane" "butene" "buteos"
   "butled" "butler" "butles" "butted" "butter" "buttes" "button" "bututs" "butyls" "buyers"
   "buying" "buyoff" "buyout" "buzuki" "buzzed" "buzzer" "buzzes" "bwanas" "byelaw" "bygone"
   "bylaws" "byline" "byname" "bypass" "bypast" "bypath" "byplay" "byrled" "byrnie" "byroad"
   "byssal" "byssus" "bytalk" "byways" "byword" "bywork" "byzant"])

(defn submit-word [w]
  (Thread/sleep 2000)
  (if (> (count w) 6)
    (generate-response {:invalid w} 500)
    (generate-response {:valid w})))

(defn rand-word []
  (generate-response (rand-nth words)))

(defroutes routes
  (GET "/" [] (index))
  (GET "/word" [] (rand-word))
  (PUT "/word" {params :params edn-params :edn-params}
    (submit-word (:word edn-params)))
  (route/files "/" {:root "resources/public"}))

(def app
  (-> routes
      wrap-edn-params))

(defonce server
  (run-jetty #'app {:port 8002 :join? false}))
