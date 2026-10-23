package com.luisvilaca.nutsandbolts

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight

// The skin: every interface colour and the typeface, in one file (see the workshop's Skin.kt for the contract).

// Midnight Big Top: a circus tent after close, the show long over and the animatronics still awake. Plum-black canvas
// ground; bulb amber is the only light (bulbs, the spotlight, gauges, a lit sign, everything pressable); canvas cream
// for text; Faz-Token pink only for tokens; blood red only for a smashed bulb (failed).
object W {
    val rail = Color(0xFF0B060D)
    val ground = Color(0xFF120A14)
    val raised = Color(0xFF2E1630) // a control on a card
    val line = Color(0xFF2B1628)
    val edge = Color(0xFF4A2541)
    val text = Color(0xFFF3E6D3)
    val dim = Color(0xFFBBA4B0) // 7:1 on a raised control
    val steel = Color(0xFFE8D5C4) // faded canvas: metal, a lit filament, selection
    val act = Color(0xFFFFC94A) // a lit bulb
    val onAct = Color(0xFF2A1404)
    val gold = Color(0xFFFF7AAE) // Faz-Tokens
    val coral = Color(0xFFFF5C5C) // a smashed bulb
    val joint = Color(0xFF7A5A6E)
    val glass = Color(0xFF0B060D)
    val card = Color(0xFF1D1020)
    val border = Color.White.copy(alpha = 0.07f)
    val sel = Color(0xFFE8D5C4).copy(alpha = 0.55f)
    val selFill = Color(0xFFE8D5C4).copy(alpha = 0.06f)
    val glow = Color(0xFFFFB938) // every glow: bulbs, the spotlight, the gauges, a fully lit sign
}

private fun face(res: Int, vararg w: Int) = FontFamily(w.map { Font(res, FontWeight(it), variationSettings = FontVariation.Settings(FontVariation.weight(it))) })
// Rye, a circus-poster wood type (OFL, licenses/), for titles and numerals; it has no Cyrillic, so Russian titles are
// set in Kelly Slab (OFL), a slab with the same poster bluntness. Body text is Manrope in every language.
private fun one(res: Int) = FontFamily(Font(res, FontWeight.Normal), Font(res, FontWeight.SemiBold), Font(res, FontWeight.Bold))
private val rye = one(R.font.rye)
private val kelly = one(R.font.kelly_slab)
private val manrope = face(R.font.manrope, 400, 500, 600)
val display get() = if (Lang.code == "ru") kelly else rye
val body get() = manrope

// The skin's icons (Icons.kt), in the circus's own grammar: round-ended strokes, and a small bulb (a ring) where the
// workshop puts a pivot joint. tickets: an admit-one stub, notched. nuts: a Faz-Token, a coin with a star. Attributes:
// a bulb (Power), a music box crank (Wiring), a bow tie (Costume), a star in a ring (Soul). Tabs: the ring (base, a
// round stage), a prize box (shop), a marquee sign (habits), a quill (diary).
val SKIN_ICONS = mapOf(
    "bolts" to "M3.5 7h17v3.2a1.8 1.8 0 0 0 0 3.6V17h-17v-3.2a1.8 1.8 0 0 0 0-3.6zM9 7v10M12.5 10.5h5M12.5 13.5h3.5",
    "nuts" to "M12 3.5a8.5 8.5 0 1 0 0 17a8.5 8.5 0 1 0 0-17zM12 7.6l1.3 2.8 3 .3-2.3 2 .7 3-2.7-1.6-2.7 1.6.7-3-2.3-2 3-.3z",
    "battery" to "M12 3.5a5.5 5.5 0 0 0-3.2 10c.6.5.9 1.2.9 2v.5h4.6v-.5c0-.8.3-1.5.9-2A5.5 5.5 0 0 0 12 3.5zM9.8 18.5h4.4M10.6 21h2.8M12 8v3",
    "processor" to "M5 9h11v9H5zM16 13.5h2.5v-4M17.2 7.8a1.3 1.3 0 1 0 2.6 0a1.3 1.3 0 1 0 -2.6 0zM8 12.5h5M8 15h3",
    "chassis" to "M12 12L4 7.5v9zM12 12l8-4.5v9zM10.6 12a1.4 1.4 0 1 0 2.8 0a1.4 1.4 0 1 0 -2.8 0z",
    "core" to "M12 3.5a8.5 8.5 0 1 0 0 17a8.5 8.5 0 1 0 0-17zM12 7l1.5 3.4 3.7.3-2.8 2.4.9 3.6L12 14.8l-3.3 1.9.9-3.6-2.8-2.4 3.7-.3z",
    "base" to "M3.5 15a8.5 3.5 0 1 0 17 0a8.5 3.5 0 1 0 -17 0zM3.5 15v2.5c0 1.9 3.8 3.5 8.5 3.5s8.5-1.6 8.5-3.5V15M12 4.5v6M9.5 6.5L12 4.5l2.5 2",
    "shop" to "M4 10h16v10H4zM3 6.5h18V10H3zM12 6.5V20M12 6.5C10 3 6.5 3.5 8 6.5M12 6.5c2-3.5 5.5-3 4 0",
    "habits" to "M3.5 6h17v10h-17zM8 16v4M16 16v4M7.2 11a1.2 1.2 0 1 0 2.4 0a1.2 1.2 0 1 0 -2.4 0zM10.8 11a1.2 1.2 0 1 0 2.4 0a1.2 1.2 0 1 0 -2.4 0zM14.4 11a1.2 1.2 0 1 0 2.4 0a1.2 1.2 0 1 0 -2.4 0z",
    "diary" to "M19.5 3.5c-6 1-11 6-12.5 13l-1.5 4M19.5 3.5c-1 4.5-4 8-8.5 10M11 9.5l2.5 2.5",
)

// The circus's words over the phrase tables (Lang.kt), per language: Bolts are Tickets, Nuts are Faz-Tokens, the
// robot is an animatronic in the ring, a habit's slots are the bulbs of its sign. Keys are the English the code writes.
// each part keyed by the workshop part it stands in for (same id, same price)
private val PARTS_WORDS = listOf(
    // heads
    "Box Mk I" to listOf("Bear Head", "Cabeça de Urso", "Голова медведя"),
    "Dome" to listOf("Bunny Head", "Cabeça de Coelho", "Голова кролика"),
    "Hex Head" to listOf("Withered Bunny", "Coelho Desgastado", "Изношенный кролик"),
    "Twin Lens" to listOf("Chicken Head", "Cabeça de Galinha", "Голова курицы"),
    "Welding Mask" to listOf("Withered Bear", "Urso Desgastado", "Изношенный медведь"),
    "Cyclops" to listOf("Pirate Fox", "Raposa Pirata", "Лис-пират"),
    "Tube TV" to listOf("Clown Doll", "Boneca Palhaça", "Кукла-клоун"),
    "Fishbowl" to listOf("Golden Bear", "Urso Dourado", "Золотой медведь"),
    "Saucer" to listOf("Puppet Mask", "Máscara da Marioneta", "Маска Марионетки"),
    // bodies
    "Crate" to listOf("Felt Suit", "Fato de Feltro", "Фетровый костюм"),
    "Capsule" to listOf("Party Bib", "Babete de Festa", "Праздничный нагрудник"),
    "Reactor" to listOf("Withered Torso", "Tronco Desgastado", "Изношенный торс"),
    "Gumball" to listOf("Clown Dress", "Vestido de Palhaça", "Платье клоунессы"),
    "Rocket" to listOf("Ringmaster Coat", "Casaca de Mestre de Pista", "Фрак шпрехшталмейстера"),
    "Coolant Tank" to listOf("Endoskeleton", "Endoesqueleto", "Эндоскелет"),
    "Tokamak" to listOf("Golden Suit", "Fato Dourado", "Золотой костюм"),
    // arms
    "Clamps" to listOf("Felt Arms", "Braços de Feltro", "Фетровые руки"),
    "Flex Hose" to listOf("Withered Arms", "Braços Desgastados", "Изношенные руки"),
    "Mitts" to listOf("Paws", "Patas", "Лапы"),
    "Wrenches" to listOf("Pirate Hook", "Gancho Pirata", "Пиратский крюк"),
    "Fins" to listOf("Endo Arms", "Braços Endo", "Эндо-руки"),
    "Magnets" to listOf("Microphone", "Microfone", "Микрофон"),
    "Sparring Gloves" to listOf("White Gloves", "Luvas Brancas", "Белые перчатки"),
    "Tentacles" to listOf("Puppet Strings", "Fios de Marioneta", "Нити марионетки"),
    "Arc Welders" to listOf("Claw Hands", "Mãos de Garra", "Руки-когти"),
    "Wings" to listOf("Juggling Pins", "Malabares", "Булавы жонглёра"),
    // legs
    "Stubs" to listOf("Felt Legs", "Pernas de Feltro", "Фетровые ноги"),
    "Springs" to listOf("Endo Legs", "Pernas Endo", "Эндо-ноги"),
    "Treads" to listOf("Withered Legs", "Pernas Desgastadas", "Изношенные ноги"),
    "Hydraulics" to listOf("Striped Stockings", "Meias às Riscas", "Полосатые чулки"),
    "Hover Jet" to listOf("Circus Ball", "Bola de Circo", "Цирковой шар"),
    "Unicycle" to listOf("Unicycle", "Monociclo", "Моноцикл"),
    "Spider Legs" to listOf("Stilts", "Andas", "Ходули"),
    "Rocket Boots" to listOf("Clown Shoes", "Sapatos de Palhaço", "Клоунские башмаки"),
    // accessories
    "Antenna" to listOf("Party Hat", "Chapéu de Festa", "Праздничный колпак"),
    "Scarf" to listOf("Ruffle Collar", "Gola de Folhos", "Жабо"),
    "Dosimeter" to listOf("Flashlight", "Lanterna", "Фонарик"),
    "Hard Hat" to listOf("Top Hat", "Cartola", "Цилиндр"),
    "Isotope Antenna" to listOf("Ears Bow", "Laço nas Orelhas", "Бант на ушах"),
    "Bow Tie" to listOf("Bow Tie", "Laço", "Галстук-бабочка"),
    "Jet Pack" to listOf("Balloon", "Balão", "Воздушный шар"),
    "Propeller" to listOf("Pinwheel Cap", "Boné de Cata-vento", "Кепка с вертушкой"),
    "Cape" to listOf("Circus Cape", "Capa de Circo", "Цирковой плащ"),
    "Top Hat & Monocle" to listOf("Eyepatch", "Pala de Pirata", "Повязка на глаз"),
    "Halo" to listOf("Halo", "Auréola", "Нимб"),
    "Crown" to listOf("Ringmaster Crown", "Coroa do Mestre de Pista", "Корона шпрехшталмейстера"),
    "Pet Moon" to listOf("Cupcake Pal", "Amigo Cupcake", "Друг-кекс"),
    "Sidekick Drone" to listOf("Lucky Balloon", "Balão da Sorte", "Счастливый шарик"),
    // the rooms
    "Charging Bay" to listOf("Center Ring", "Picadeiro Central", "Центральный манеж"),
    "Workshop" to listOf("Parts & Service", "Peças e Serviço", "Запчасти и сервис"),
    "Observatory" to listOf("Pirate Cove", "Enseada Pirata", "Пиратская бухта"),
    "Star Deck" to listOf("Show Stage", "Palco do Espetáculo", "Сцена шоу"),
    "Reactor Hall" to listOf("Circus Gallery", "Galeria do Circo", "Цирковая галерея"),
)

private val STAGES = listOf(
    // Power
    "AA Cell" to listOf("Flicker", "Tremeluz", "Мерцание"), "Nickel Pack" to listOf("Night Light", "Luz de Presença", "Ночник"),
    "Lithium Cell" to listOf("Flashlight", "Lanterna", "Фонарик"), "Li-ion Bank" to listOf("Backup Battery", "Bateria de Reserva", "Запасная батарея"),
    "Solid-State" to listOf("Generator", "Gerador", "Генератор"), "Supercapacitor" to listOf("Spotlight", "Holofote", "Прожектор"),
    "Graphene Cell" to listOf("Floodlights", "Projetores", "Софиты"), "Fusion Cell" to listOf("Stage Lights", "Luzes de Palco", "Огни сцены"),
    "Zero-Point Cell" to listOf("Grand Marquee", "Grande Letreiro", "Большая вывеска"), "Antimatter Cell" to listOf("6 AM", "6 da Manhã", "6 утра"),
    // Wiring
    "4-bit" to listOf("Loose Wires", "Fios Soltos", "Оборванные провода"), "8-bit" to listOf("Tape Recorder", "Gravador", "Магнитофон"),
    "16-bit" to listOf("Music Box", "Caixa de Música", "Музыкальная шкатулка"), "32-bit" to listOf("Voice Box", "Caixa de Voz", "Голосовой модуль"),
    "64-bit" to listOf("Show Mode", "Modo Espetáculo", "Режим шоу"), "Multicore" to listOf("Free Roam", "Modo Livre", "Свободный режим"),
    "Neural Engine" to listOf("Night Mode", "Modo Noturno", "Ночной режим"), "Quantum Core" to listOf("Learning Chip", "Chip de Aprendizagem", "Обучаемый чип"),
    "Photonic Mind" to listOf("Hive Mind", "Mente Coletiva", "Коллективный разум"), "Singularity" to listOf("Mastermind", "Mente Mestra", "Кукловод"),
    // Costume
    "Tin Can" to listOf("Torn Felt", "Feltro Rasgado", "Рваный фетр"), "Aluminium" to listOf("Patched Suit", "Fato Remendado", "Залатанный костюм"),
    "Steel" to listOf("Withered", "Desgastado", "Изношенный"), "Titanium" to listOf("Restored", "Restaurado", "Восстановленный"),
    "Carbon Fibre" to listOf("Toy Plastic", "Plástico de Brinquedo", "Игрушечный пластик"), "Tungsten" to listOf("Funtime Finish", "Acabamento Funtime", "Отделка Фантайм"),
    "Graphene" to listOf("Showroom Shine", "Brilho de Montra", "Блеск витрины"), "Nanoweave" to listOf("Glamrock", "Glamrock", "Гламрок"),
    "Diamond Lattice" to listOf("Stage Star", "Estrela do Palco", "Звезда сцены"), "Neutronium" to listOf("Golden Suit", "Fato Dourado", "Золотой костюм"),
    // Soul
    "Spark" to listOf("Faint Glow", "Brilho Ténue", "Слабое свечение"), "Dynamo" to listOf("Spark", "Faísca", "Искра"),
    "Turbine" to listOf("Puppet String", "Fio de Marioneta", "Нить марионетки"), "Fission Pile" to listOf("Gift Box", "Caixa de Presente", "Подарочная коробка"),
    "Breeder" to listOf("Lullaby", "Canção de Embalar", "Колыбельная"), "Tokamak" to listOf("Remnant", "Remanescente", "Остаток"),
    "Fusion Heart" to listOf("Haunted", "Assombrado", "Одержимый"), "Stellarator" to listOf("Bound Soul", "Alma Presa", "Связанная душа"),
    "Star Core" to listOf("Happiest Day", "O Dia Mais Feliz", "Самый счастливый день"), "Supernova" to listOf("Joy of Creation", "Alegria da Criação", "Радость творения"),
)

private val PHRASES = listOf(
    "Lab" to listOf("The Ring", "O Picadeiro", "Манеж"),
    "Shop" to listOf("Prize Corner", "Canto dos Prémios", "Призы"),
    "Habits" to listOf("Acts", "Números", "Номера"),
    "Diary" to listOf("Night Log", "Diário Noturno", "Журнал ночи"),
    "Robot" to listOf("Animatronic", "Animatrónico", "Аниматроник"),
    "Base" to listOf("Stage", "Palco", "Сцена"),
    "Rivet" to listOf("Freddy", "Freddy", "Фредди"),
    "Battery" to listOf("Power", "Energia", "Энергия"),
    "Processor" to listOf("Wiring", "Circuitos", "Проводка"),
    "Chassis" to listOf("Costume", "Fato", "Костюм"),
    "Core" to listOf("Soul", "Alma", "Душа"),
    "Low power" to listOf("Withered", "Desgastado", "Изношен"),
    "Running steady" to listOf("On stage", "Em palco", "На сцене"),
    "Fully charged" to listOf("Showtime!", "Hora do espetáculo!", "Время шоу!"),
    "Take back the last bolt" to listOf("Take back the last ticket", "Retirar o último bilhete", "Вернуть последний билет"),
    "Bolt taken back" to listOf("Ticket taken back", "Bilhete retirado", "Билет возвращён"),
    "Note on the last bolt" to listOf("Note on the last ticket", "Nota no último bilhete", "Заметка к последнему билету"),
    "Its frame comes off the bench. What it already earned stays." to listOf("Its sign comes down from the ring. What it already earned stays.", "O letreiro sai do picadeiro. O que já ganhou fica.", "Его вывеска снимается с манежа. Заработанное остаётся."),
    " · +1 nut for the streak" to listOf(" · +1 Faz-Token for the streak", " · +1 Faz-Token pela sequência", " · +1 фаз-жетон за серию"),
    "Sheared" to listOf("Smashed", "Partida", "Разбита"),
    "sheared" to listOf("smashed", "partidas", "разбито"),
    "{0} sheared" to listOf("{0} smashed", "{0} partidas", "разбито: {0}"),
    "The bench is empty" to listOf("The ring is empty", "O picadeiro está vazio", "Манеж пуст"),
    "Each habit becomes a frame on {0}'s bench. Every time you do it, a bolt goes in." to listOf(
        "Each habit becomes a sign over {0}'s ring. Every time you do it, a bulb lights up.",
        "Cada hábito torna-se um letreiro no picadeiro de {0}. Sempre que o cumpre, acende-se uma lâmpada.",
        "Каждая привычка становится вывеской над манежем {0}. Каждый раз, когда вы её выполняете, загорается лампочка."),
    "{0} of {1} in" to listOf("{0} of {1} lit", "{0} de {1} acesas", "горит {0} из {1}"),
    "bolts in" to listOf("tickets in", "bilhetes ganhos", "билетов получено"),
    "BOLTS" to listOf("TICKETS", "BILHETES", "БИЛЕТЫ"),
    "A nut for every week completed." to listOf("A Faz-Token for every week completed.", "Um Faz-Token por cada semana completa.", "Фаз-жетон за каждую завершённую неделю."),
    "A nut for every third day in a row." to listOf("A Faz-Token for every third day in a row.", "Um Faz-Token a cada três dias seguidos.", "Фаз-жетон за каждый третий день подряд."),
    "A notification at this time on the days it runs, while it still has open bolts. Its {0} button logs it without opening the app." to listOf(
        "A notification at this time on the days it runs, while its sign still has dark bulbs. Its {0} button logs it without opening the app.",
        "Uma notificação a esta hora nos dias em que corre, enquanto o letreiro ainda tiver lâmpadas apagadas. O botão {0} regista-o sem abrir a app.",
        "Уведомление в это время в дни, когда привычка идёт, пока на её вывеске есть непогашенные лампочки. Кнопка {0} отмечает её, не открывая приложение."),
    "Notifications are off for Nuts And Bolts, so the reminder cannot show." to listOf(
        "Notifications are off for Five Nights at the Circus, so the reminder cannot show.",
        "As notificações do Five Nights at the Circus estão desligadas, por isso o lembrete não pode aparecer.",
        "Уведомления для Five Nights at the Circus выключены, поэтому напоминание не может появиться."),
    "That file is not a Nuts And Bolts backup." to listOf("That file is not a Five Nights at the Circus backup.", "Esse ficheiro não é uma cópia do Five Nights at the Circus.", "Этот файл не является копией Five Nights at the Circus."),
    "One file with every habit, bolt, part and diary page. Keep a copy off this device, in Drive for example." to listOf(
        "One file with every habit, ticket, part and diary page. Keep a copy off this device, in Drive for example.",
        "Um ficheiro com todos os hábitos, bilhetes, peças e páginas do diário. Guarde uma cópia fora deste dispositivo, no Drive, por exemplo.",
        "Один файл со всеми привычками, билетами, деталями и страницами дневника. Храните копию вне этого устройства, например на Диске."),
    "Everything on this device is replaced with the backup{0}. Habits, robot and diary pages written since then are lost." to listOf(
        "Everything on this device is replaced with the backup{0}. Habits, animatronic and diary pages written since then are lost.",
        "Tudo neste dispositivo é substituído pela cópia{0}. Os hábitos, o animatrónico e as páginas escritas desde então perdem-se.",
        "Всё на этом устройстве заменяется копией{0}. Привычки, аниматроник и страницы дневника, написанные с тех пор, будут потеряны."),
    "The workshop's own sounds, at the media volume: a bolt driven home, a frame going critical, a sheared bolt, a level gained or lost, a nut, a purchase, a part fitted, a habit added or taken off the bench, and {0} answering a tap." to listOf(
        "The circus after dark, at the media volume: a bulb buzzing on, a sign lighting up with a fanfare, a bulb smashing, a level gained or lost, a Faz-Token, a prize won, a part fitted, a sign hung or taken down, and {0} answering a tap.",
        "O circo depois do fecho, ao volume multimédia: uma lâmpada a acender, um letreiro todo aceso com fanfarra, uma lâmpada a partir, um nível ganho ou perdido, um Faz-Token, um prémio, uma peça montada, um letreiro pendurado ou retirado, e {0} a responder a um toque.",
        "Цирк после закрытия, на громкости мультимедиа: зажигающаяся лампочка, вывеска, загоревшаяся под фанфары, разбитая лампочка, полученный или потерянный уровень, фаз-жетон, приз, установленная деталь, повешенная или снятая вывеска, и {0}, отвечающий на касание."),
    "Sign in with the same Google account on the tablet and the phone, and both keep the same habits, robot and diary pages. Changes made offline go across once the device is back online." to listOf(
        "Sign in with the same Google account on each device, and all keep the same habits, animatronic and diary pages. Changes made offline go across once the device is back online.",
        "Inicie sessão com a mesma conta Google em cada dispositivo, e todos ficam com os mesmos hábitos, animatrónico e páginas do diário. As alterações feitas sem rede passam quando o dispositivo voltar a estar online.",
        "Войдите в один и тот же аккаунт Google на каждом устройстве, и на всех будут одни и те же привычки, аниматроник и страницы дневника. Изменения без сети отправятся, когда устройство снова будет онлайн."),
    "First bolt" to listOf("First ticket", "Primeiro bilhete", "Первый билет"),
    "A whole day critical" to listOf("A whole night lit up", "Uma noite toda acesa", "Вся ночь в огнях"),
    "500 bolts earned" to listOf("500 tickets earned", "500 bilhetes ganhos", "500 билетов получено"),
)

val SKIN_WORDS: Map<String, Map<String, String>> = listOf("en", "pt", "ru").mapIndexed { i, code ->
    // the circus's own parts (Parts.kt) are written in English under their new names, so those are the keys
    code to (PARTS_WORDS.map { (_, v) -> v[0] to v } + STAGES + PHRASES).associate { (k, v) -> k to v[i] }
}.toMap()

val SKIN_NOUNS: Map<String, Map<String, List<String>>> = mapOf(
    "bolt" to mapOf("en" to listOf("ticket", "tickets"), "pt" to listOf("bilhete", "bilhetes"), "ru" to listOf("билет", "билета", "билетов")),
    "nut" to mapOf("en" to listOf("Faz-Token", "Faz-Tokens"), "pt" to listOf("Faz-Token", "Faz-Tokens"), "ru" to listOf("фаз-жетон", "фаз-жетона", "фаз-жетонов")),
    "sheared" to mapOf("en" to listOf("smashed"), "pt" to listOf("partida", "partidas"), "ru" to listOf("разбита", "разбиты", "разбито")),
)

// which look this build wears (the buddy card tells the other app)
const val SKIN = "circus"
