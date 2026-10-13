'use strict';
// Robot parts and the bay scene, all drawn as SVG strings.
// Robot space is 400x520 with the feet landing at y=468. Arms and legs are drawn once and mirrored.

const mirror = s => `<g transform="translate(400 0) scale(-1 1)">${s}</g>`;
const both = s => s + mirror(s);
const arms = s => `<g class="arm arm-l">${s}</g><g class="arm arm-r">${mirror(s)}</g>`;
const HEAD_BOX = [96, 70, 208, 152], BODY_BOX = [106, 222, 188, 158], ARM_BOX = [70, 240, 260, 130], LEG_BOX = [100, 350, 200, 126];
const fish = (x, y, cls = 'cr') => `<path class="${cls}" d="M${x} ${y}q14-12 28 0q-14 12-28 0zM${x + 28} ${y}l10-8v16z"/>`;

const PARTS = [
  // heads
  { id: 'head-box', slot: 'head', name: 'Box Mk I', no: 'NB-H01', box: HEAD_BOX, svg: `
    <circle class="zn" cx="113" cy="152" r="10"/><circle class="zn" cx="287" cy="152" r="10"/>
    <rect class="sh" x="116" y="94" width="168" height="116" rx="30"/>
    <rect class="gl" x="132" y="116" width="136" height="68" rx="24"/>
    <path class="hl" d="M150 130q14-6 34-6"/>
    <g class="eyes"><rect class="ey" x="161" y="134" width="22" height="30" rx="11"/><rect class="ey" x="217" y="134" width="22" height="30" rx="11"/></g>` },
  { id: 'head-dome', slot: 'head', name: 'Dome', no: 'NB-H02', bolts: 120, box: HEAD_BOX, svg: `
    <path class="sh" d="M110 186C110 118 150 82 200 82s90 36 90 104c0 16-12 24-28 24H138c-16 0-28-8-28-24z"/>
    <path class="gl" d="M130 160c0-26 30-38 70-38s70 12 70 38c0 22-30 32-70 32s-70-10-70-32z"/>
    <path class="hl" d="M152 138q20-9 48-9"/>
    <g class="eyes"><ellipse class="ey" cx="172" cy="159" rx="15" ry="11" transform="rotate(-10 172 159)"/><ellipse class="ey" cx="228" cy="159" rx="15" ry="11" transform="rotate(10 228 159)"/></g>` },
  { id: 'head-hex', slot: 'head', name: 'Hex Head', no: 'NB-H04', bolts: 150, nuts: 3, box: HEAD_BOX, svg: `
    <path class="sh shr" d="M130 150l35-60h70l35 60-35 60h-70z"/>
    <circle class="gl" cx="200" cy="150" r="45"/>
    <path class="hl" d="M172 128q12-10 28-11"/>
    <g class="eyes"><circle class="ey" cx="182" cy="152" r="11"/><circle class="ey" cx="218" cy="152" r="11"/></g>` },
  { id: 'head-binoc', slot: 'head', name: 'Twin Lens', no: 'NB-H03', bolts: 200, box: HEAD_BOX, svg: `
    <rect class="zn" x="180" y="170" width="40" height="42" rx="8"/><rect class="zn" x="182" y="130" width="36" height="26" rx="6"/>
    <rect class="sh" x="106" y="102" width="90" height="84" rx="36" transform="rotate(-6 151 144)"/>
    <rect class="sh" x="204" y="102" width="90" height="84" rx="36" transform="rotate(6 249 144)"/>
    <circle class="gl" cx="152" cy="144" r="30"/><circle class="gl" cx="248" cy="144" r="30"/>
    <g class="eyes"><circle class="ey" cx="156" cy="147" r="13"/><circle class="ey" cx="244" cy="147" r="13"/></g>` },
  { id: 'head-cyclops', slot: 'head', name: 'Cyclops', no: 'NB-H05', bolts: 260, nuts: 2, box: HEAD_BOX, svg: `
    <rect class="sh" x="122" y="92" width="156" height="120" rx="46"/>
    <circle class="zn" cx="200" cy="150" r="47"/><circle class="gl" cx="200" cy="150" r="39"/>
    <path class="hl" d="M176 130q10-10 24-11"/>
    <g class="eyes"><circle class="ey" cx="200" cy="152" r="17"/></g>` },
  { id: 'head-monitor', slot: 'head', name: 'Tube TV', no: 'NB-H06', bolts: 340, nuts: 5, box: [96, 50, 208, 172], svg: `
    <path class="rod" d="M178 98l-24-40M222 98l24-40"/>
    <rect class="sh" x="106" y="94" width="188" height="118" rx="18"/>
    <rect class="gl" x="120" y="108" width="130" height="90" rx="14"/>
    <g class="eyes"><rect class="ey" x="152" y="130" width="18" height="26" rx="4"/><rect class="ey" x="200" y="130" width="18" height="26" rx="4"/></g>
    <path class="smile" d="M164 174q21 12 42 0"/>
    <circle class="zn" cx="272" cy="124" r="8"/><circle class="zn" cx="272" cy="150" r="8"/><path class="ln" d="M264 176h16M264 187h16"/>` },
  { id: 'head-bowl', slot: 'head', name: 'Fishbowl', no: 'NB-H07', bolts: 300, nuts: 6, box: [96, 62, 208, 160], svg: `
    <rect class="zn" x="158" y="194" width="84" height="20" rx="7"/>
    <circle class="bowl" cx="200" cy="138" r="66"/>
    <path class="water" d="M139 150q20-9 40 0t40 0t42 0a66 66 0 0 1-122 0z"/>
    <g class="eyes"><circle class="ey" cx="180" cy="130" r="10"/><circle class="ey" cx="220" cy="130" r="10"/></g>
    ${fish(206, 174)}
    <circle class="bub" cx="182" cy="170" r="4"/><circle class="bub" cx="172" cy="184" r="2.5"/>
    <path class="hl" d="M156 106q14-18 36-22"/>` },
  { id: 'head-saucer', slot: 'head', name: 'Saucer', no: 'NB-H08', bolts: 420, nuts: 8, box: [84, 60, 232, 160], svg: `
    <rect class="zn" x="184" y="176" width="32" height="34" rx="6"/>
    <path class="bowl" d="M152 136a48 48 0 0 1 96 0z"/>
    <circle class="leaf" cx="200" cy="118" r="13"/><path class="rod" d="M200 105v-9"/><circle class="gl" cx="195" cy="117" r="2.5"/><circle class="gl" cx="205" cy="117" r="2.5"/>
    <ellipse class="sh" cx="200" cy="152" rx="106" ry="34"/>
    <ellipse class="gl" cx="200" cy="148" rx="62" ry="17"/>
    <g class="eyes"><ellipse class="ey" cx="180" cy="148" rx="9" ry="7"/><ellipse class="ey" cx="220" cy="148" rx="9" ry="7"/></g>
    <g class="pulse"><circle class="br" cx="122" cy="168" r="6"/><circle class="br" cx="160" cy="179" r="6"/><circle class="br" cx="200" cy="183" r="6"/><circle class="br" cx="240" cy="179" r="6"/><circle class="br" cx="278" cy="168" r="6"/></g>` },

  // bodies
  { id: 'body-crate', slot: 'body', name: 'Crate', no: 'NB-B01', box: BODY_BOX, svg: `
    <rect class="sh" x="126" y="232" width="148" height="138" rx="28"/>
    <rect class="sh2" x="148" y="256" width="104" height="54" rx="12"/>
    <circle class="ey pulse" cx="170" cy="283" r="8"/>
    <path class="ln" d="M194 272h42M194 283h42M194 294h28M152 336h96"/>` },
  { id: 'body-capsule', slot: 'body', name: 'Capsule', no: 'NB-B02', bolts: 140, box: BODY_BOX, svg: `
    <path class="sh" d="M130 262c0-22 22-30 70-30s70 8 70 30l-6 66c-4 28-26 44-64 44s-60-16-64-44z"/>
    <circle class="gl" cx="200" cy="288" r="28"/>
    <circle class="ring spin" cx="200" cy="288" r="17"/>` },
  { id: 'body-reactor', slot: 'body', name: 'Reactor', no: 'NB-B03', bolts: 180, nuts: 4, box: BODY_BOX, svg: `
    <path class="sh" d="M116 248q0-16 16-16h136q16 0 16 16l-12 106q-2 16-18 16H146q-16 0-18-16z"/>
    <circle class="br" cx="200" cy="294" r="33"/><circle class="gl" cx="200" cy="294" r="25"/>
    <circle class="ey pulse" cx="200" cy="294" r="12"/>
    <path class="ln" d="M134 258h22M135 271h20M244 258h22M245 271h20"/>` },
  { id: 'body-gumball', slot: 'body', name: 'Gumball', no: 'NB-B04', bolts: 380, nuts: 8, box: BODY_BOX, svg: `
    <rect class="zn" x="118" y="258" width="164" height="12" rx="6"/>
    <path class="cr" d="M152 332h96l16 38H136z"/><rect class="gl" x="184" y="344" width="32" height="16" rx="4"/>
    <circle class="bowl" cx="200" cy="286" r="60"/>
    <circle class="br" cx="170" cy="310" r="11"/><circle class="cr" cx="194" cy="318" r="11"/><circle class="mt" cx="220" cy="314" r="11"/><circle class="ice" cx="240" cy="298" r="11"/>
    <circle class="mt" cx="158" cy="290" r="11"/><circle class="ice" cx="182" cy="296" r="11"/><circle class="br" cx="206" cy="294" r="11"/><circle class="cr" cx="226" cy="280" r="11"/>
    <circle class="cr" cx="172" cy="274" r="11"/><circle class="br" cx="196" cy="272" r="11"/>
    <path class="hl" d="M160 258q14-18 36-22"/>` },
  { id: 'body-rocket', slot: 'body', name: 'Rocket', no: 'NB-B05', bolts: 400, nuts: 9, box: [96, 222, 208, 162], svg: `
    <path class="cr" d="M132 322l-28 54h40zM268 322l28 54h-40z"/>
    <path class="sh" d="M132 252q0-20 68-20t68 20v94q0 24-68 24t-68-24z"/>
    <rect class="cr" x="132" y="326" width="136" height="14"/>
    <circle class="zn" cx="200" cy="284" r="31"/><circle class="gl" cx="200" cy="284" r="23"/><circle class="ey pulse" cx="200" cy="284" r="9"/>` },
  { id: 'body-tank', slot: 'body', name: 'Aquarium', no: 'NB-B06', bolts: 450, nuts: 10, box: BODY_BOX, svg: `
    <rect class="zn" x="118" y="258" width="164" height="12" rx="6"/>
    <rect class="bowl" x="132" y="246" width="136" height="110"/>
    <path class="water" d="M134 268q17-8 34 0t34 0t34 0t30 0v86H134z"/>
    <path class="stem" d="M152 352q-8-14 0-26t0-22"/><path class="stem" d="M248 352q8-12 0-22"/>
    ${fish(170, 312)}<path class="br" d="M246 290q-11-9-22 0q11 9 22 0zM224 290l-8-6v12z"/>
    <circle class="bub" cx="214" cy="334" r="4"/><circle class="bub" cx="222" cy="320" r="2.5"/>
    <rect class="sh" x="124" y="232" width="152" height="20" rx="10"/><rect class="sh" x="124" y="350" width="152" height="20" rx="10"/>` },

  // arms (left arm; shoulder at 120,264)
  { id: 'arms-clamp', slot: 'arms', name: 'Clamps', no: 'NB-A01', box: ARM_BOX, svg: arms(`
    <circle class="zn" cx="120" cy="264" r="14"/>
    <rect class="sh" x="90" y="256" width="30" height="72" rx="15"/>
    <path class="claw" d="M96 330q-8 18 6 28M114 330q8 18-6 28"/>`) },
  { id: 'arms-flex', slot: 'arms', name: 'Flex Hose', no: 'NB-A02', bolts: 80, box: ARM_BOX, svg: arms(`
    <circle class="zn" cx="120" cy="264" r="14"/>
    <rect class="sh2" x="92" y="258" width="26" height="18" rx="9"/><rect class="sh" x="92" y="279" width="26" height="18" rx="9"/>
    <rect class="sh2" x="92" y="300" width="26" height="18" rx="9"/><rect class="sh" x="92" y="321" width="26" height="18" rx="9"/>
    <circle class="sh" cx="105" cy="354" r="15"/>`) },
  { id: 'arms-mitt', slot: 'arms', name: 'Mitts', no: 'NB-A03', bolts: 110, box: ARM_BOX, svg: arms(`
    <circle class="zn" cx="120" cy="264" r="12"/>
    <rect class="zn" x="100" y="262" width="11" height="60" rx="5"/>
    <circle class="sh" cx="124" cy="330" r="9"/><ellipse class="sh" cx="105" cy="340" rx="21" ry="24"/>`) },
  { id: 'arms-fin', slot: 'arms', name: 'Fins', no: 'NB-A04', bolts: 100, nuts: 2, box: ARM_BOX, svg: arms(`
    <path class="sh" d="M112 248c-26 20-32 76-14 114 18-24 26-74 22-112z"/>`) },
  { id: 'arms-magnet', slot: 'arms', name: 'Magnets', no: 'NB-A05', bolts: 220, nuts: 4, box: ARM_BOX, svg: arms(`
    <circle class="zn" cx="120" cy="264" r="13"/>
    <rect class="zn" x="100" y="262" width="11" height="58" rx="5"/>
    <path class="mag" d="M90 356v-20a16 16 0 0 1 32 0v20"/><path class="magtip" d="M90 356v-10M122 356v-10"/>`) },
  { id: 'arms-glove', slot: 'arms', name: 'Sparring Gloves', no: 'NB-A06', bolts: 240, nuts: 4, box: ARM_BOX, svg: arms(`
    <circle class="zn" cx="120" cy="264" r="13"/>
    <path class="spring" d="M108 270l-10 7 20 8-20 8 20 8-10 7"/>
    <circle class="cr" cx="131" cy="330" r="9"/>
    <path class="cr" d="M84 324q0-10 14-10h18q12 0 12 12v20q0 14-16 14H98q-14 0-14-14z"/>
    <rect class="sh" x="88" y="306" width="36" height="11" rx="5"/>`) },
  { id: 'arms-tentacle', slot: 'arms', name: 'Tentacles', no: 'NB-A07', bolts: 280, nuts: 5, box: [50, 240, 300, 136], svg: arms(`
    <path class="tent" d="M118 262q-30 14-22 44t-10 44q-6 12-22 8"/>
    <circle class="zn" cx="103" cy="291" r="3.5"/><circle class="zn" cx="97" cy="316" r="3.500"/><circle class="zn" cx="88" cy="340" r="3.500"/>`) },
  { id: 'arms-wing', slot: 'arms', name: 'Wings', no: 'NB-A08', bolts: 500, nuts: 12, box: [20, 176, 360, 170], svg: arms(`
    <path class="sh2" d="M116 268 28 248q6 38 38 46z"/>
    <path class="sh" d="M118 260 40 188q-8 44 20 66z"/>
    <path class="sh" d="M114 276l-70 34q18 26 46 18z"/>
    <circle class="zn" cx="120" cy="266" r="12"/>`) },

  // legs (left leg; hips at 168 and 232)
  { id: 'legs-stub', slot: 'legs', name: 'Stubs', no: 'NB-L01', box: LEG_BOX, svg: both(`
    <rect class="zn" x="158" y="356" width="22" height="84" rx="8"/>
    <rect class="sh" x="140" y="432" width="56" height="36" rx="18"/>`) },
  { id: 'legs-spring', slot: 'legs', name: 'Springs', no: 'NB-L02', bolts: 90, box: LEG_BOX, svg: both(`
    <path class="spring" d="M169 362l-12 10 24 12-24 12 24 12-24 12 12 10"/>
    <ellipse class="sh" cx="169" cy="452" rx="28" ry="16"/>`) },
  { id: 'legs-tread', slot: 'legs', name: 'Treads', no: 'NB-L03', bolts: 160, box: LEG_BOX, svg: `
    <rect class="zn" x="146" y="358" width="108" height="34" rx="14"/>` + both(`
    <rect class="gl" x="108" y="388" width="78" height="80" rx="34"/><rect class="tread" x="108" y="388" width="78" height="80" rx="34"/>
    <circle class="sh" cx="147" cy="412" r="11"/><circle class="sh" cx="147" cy="446" r="11"/>`) },
  { id: 'legs-hover', slot: 'legs', name: 'Hover Jet', no: 'NB-L04', bolts: 220, nuts: 5, box: LEG_BOX, svg: `
    <path class="zn" d="M166 364h68l-14 34h-40z"/>
    <ellipse class="ey pulse" cx="200" cy="410" rx="22" ry="9"/>
    <ellipse class="ring pulse" cx="200" cy="452" rx="62" ry="12"/>` },
  { id: 'legs-wheel', slot: 'legs', name: 'Unicycle', no: 'NB-L05', bolts: 260, nuts: 5, box: LEG_BOX, svg: `
    <path class="zn" d="M184 358h32l-6 60h-20z"/>
    <circle class="gl" cx="200" cy="426" r="42"/><path class="ln" d="M200 388v76M162 426h76"/>
    <circle class="tread" cx="200" cy="426" r="42"/><circle class="sh" cx="200" cy="426" r="14"/>` },
  { id: 'legs-spider', slot: 'legs', name: 'Spider Legs', no: 'NB-L06', bolts: 340, nuts: 7, box: [76, 350, 248, 126], svg: `
    <rect class="zn" x="150" y="358" width="100" height="28" rx="13"/>` + both(`
    <path class="claw" d="M162 374l-46 22-10 66M178 382l-18 32-4 48"/>
    <circle class="zn" cx="116" cy="396" r="7"/><circle class="zn" cx="160" cy="414" r="7"/>
    <circle class="sh" cx="106" cy="461" r="8"/><circle class="sh" cx="156" cy="461" r="8"/>`) },
  { id: 'legs-boots', slot: 'legs', name: 'Rocket Boots', no: 'NB-L07', bolts: 420, nuts: 9, box: LEG_BOX, svg: both(`
    <rect class="zn" x="158" y="356" width="22" height="48" rx="8"/>
    <path class="sh" d="M146 396h46l6 44h-58z"/><rect class="cr" x="143" y="410" width="52" height="10"/>
    <path class="zn" d="M150 440h38l-5 10h-28z"/>
    <path class="br pulse" d="M157 450q12 32 24 0z"/>`) },

  // accessories; `at` says which layer they ride on
  { id: 'acc-antenna', slot: 'acc', at: 'head-back', name: 'Antenna', no: 'NB-X01', bolts: 40, box: [190, 30, 110, 100], svg: `
    <path class="rod" d="M236 112l16-56"/><circle class="ey pulse" cx="253" cy="50" r="9"/>` },
  { id: 'acc-scarf', slot: 'acc', at: 'front', name: 'Scarf', no: 'NB-X02', bolts: 50, box: [140, 208, 120, 94], svg: `
    <path class="scarf" d="M168 240l-14 50 26-8 6-38z"/>
    <path class="scarf" d="M158 218q42 24 84 0l4 18q-46 26-92 0z"/>` },
  { id: 'acc-sprout', slot: 'acc', at: 'head-back', name: 'Sprout', no: 'NB-X03', bolts: 60, box: [140, 20, 120, 128], svg: `
    <path class="stem" d="M200 140V62"/>
    <path class="leaf" d="M200 72c-24-2-34-18-32-30 20 2 32 14 32 30z"/><path class="leaf" d="M200 62c22 0 34-14 34-28-20 0-32 12-34 28z"/>` },
  { id: 'acc-bow', slot: 'acc', at: 'front', name: 'Bow Tie', no: 'NB-X06', bolts: 70, box: [160, 196, 80, 60], svg: `
    <path class="cr" d="M200 226l-28-15v30zM200 226l28-15v30z"/><circle class="br" cx="200" cy="226" r="6"/>` },
  { id: 'acc-pack', slot: 'acc', at: 'body-back', name: 'Jet Pack', no: 'NB-X04', bolts: 150, nuts: 2, box: [126, 196, 148, 76], svg: `
    <rect class="zn" x="136" y="206" width="42" height="56" rx="14"/><rect class="zn" x="222" y="206" width="42" height="56" rx="14"/>
    <rect class="br" x="141" y="206" width="32" height="12" rx="6"/><rect class="br" x="227" y="206" width="32" height="12" rx="6"/>` },
  { id: 'acc-prop', slot: 'acc', at: 'head-back', name: 'Propeller', no: 'NB-X07', bolts: 180, nuts: 3, box: [140, 24, 120, 110], svg: `
    <path class="rod" d="M200 124V56"/>
    <g class="prop"><ellipse class="cr" cx="174" cy="54" rx="26" ry="7"/><ellipse class="br" cx="226" cy="54" rx="26" ry="7"/></g>
    <circle class="zn" cx="200" cy="54" r="6"/>` },
  { id: 'acc-cape', slot: 'acc', at: 'body-back', name: 'Cape', no: 'NB-X08', bolts: 260, nuts: 5, box: [60, 226, 280, 170], svg: `
    <path class="cr" d="M152 238h96l84 124q-132 28-264 0z"/>` },
  { id: 'acc-hat', slot: 'acc', at: 'head-front', name: 'Top Hat & Monocle', no: 'NB-X09', bolts: 300, nuts: 6, box: [140, 24, 130, 190], svg: `
    <rect class="gl" x="166" y="32" width="68" height="54" rx="5"/><rect class="cr" x="166" y="68" width="68" height="10"/>
    <rect class="gl" x="146" y="82" width="108" height="11" rx="5"/>
    <circle class="mono" cx="228" cy="150" r="21"/><path class="chain" d="M247 160q14 18 4 44"/>` },
  { id: 'acc-halo', slot: 'acc', at: 'head-front', name: 'Halo', no: 'NB-X05', nuts: 6, box: [140, 36, 120, 54], svg: `
    <ellipse class="halo" cx="200" cy="62" rx="48" ry="11"/>` },
  { id: 'acc-crown', slot: 'acc', at: 'head-front', name: 'Crown', no: 'NB-X10', nuts: 12, box: [146, 30, 108, 70], svg: `
    <path class="br" d="M162 86l-8-36 24 18 22-26 22 26 24-18-8 36z"/>
    <circle class="cr" cx="200" cy="72" r="5"/><circle class="mt" cx="178" cy="75" r="3.500"/><circle class="mt" cx="222" cy="75" r="3.500"/>` },
  { id: 'acc-moon', slot: 'acc', at: 'front', name: 'Pet Moon', no: 'NB-X11', nuts: 15, box: [80, 80, 240, 140], svg: `
    <g transform="rotate(-14 200 150)"><ellipse class="orbit" cx="200" cy="150" rx="112" ry="34"/>
    <circle class="moon" r="10"><animateMotion dur="7s" repeatCount="indefinite" path="M88 150a112 34 0 1 0 224 0a112 34 0 1 0-224 0"/></circle></g>` },
  { id: 'acc-drone', slot: 'acc', at: 'front', name: 'Sidekick Drone', no: 'NB-X12', nuts: 20, box: [292, 108, 80, 72], svg: `
    <g class="hover"><path class="rod" d="M310 132h44"/><ellipse class="sh2" cx="310" cy="129" rx="15" ry="4"/><ellipse class="sh2" cx="354" cy="129" rx="15" ry="4"/>
    <rect class="sh" x="313" y="135" width="38" height="30" rx="11"/><rect class="gl" x="319" y="142" width="26" height="13" rx="6"/>
    <circle class="ey" cx="327" cy="148.500" r="3"/><circle class="ey" cx="337" cy="148.500" r="3"/></g>` },

  // base evolutions; each tier keeps everything below it
  { id: 'base-bay', slot: 'base', tier: 1, name: 'Charging Bay', no: 'NB-Z01' },
  { id: 'base-shop', slot: 'base', tier: 2, name: 'Workshop', no: 'NB-Z02', nuts: 4 },
  { id: 'base-dome', slot: 'base', tier: 3, name: 'Observatory', no: 'NB-Z03', nuts: 10 },
  { id: 'base-deck', slot: 'base', tier: 4, name: 'Star Deck', no: 'NB-Z04', nuts: 25 },
];
const PART = Object.fromEntries(PARTS.map(p => [p.id, p]));
const SLOTS = { head: 'Head', body: 'Body', arms: 'Arms', legs: 'Legs', acc: 'Accessories', base: 'Base' };

// attrs lets the scene place the robot as a nested <svg>; cls carries mood.
// The legs stay planted while .bob breathes above them; .all moves everything (the cheer hop).
function robotSVG(equip, attrs = '', cls = '') {
  const p = slot => PART[equip[slot]]?.svg || '';
  const acc = PART[equip.acc], at = k => acc?.at === k ? acc.svg : '';
  return `<svg class="bot ${cls}" viewBox="0 0 400 520" ${attrs} aria-hidden="true">
    <ellipse class="shadow" cx="200" cy="474" rx="92" ry="12"/>
    <g class="all">
      ${p('legs')}
      <g class="bob">
        ${at('body-back')}
        <rect class="zn" x="187" y="198" width="26" height="42" rx="6"/>
        ${p('body')}
        <g class="head">${at('head-back')}${p('head')}${at('head-front')}</g>
        ${p('arms')}
        ${at('front')}
      </g>
    </g></svg>`;
}

// One part on its own, square-cropped for a shop tile.
function partThumb(p) {
  if (p.slot === 'base') return sceneSVG(p.id, null);
  const [x, y, w, h] = p.box, s = Math.max(w, h) * 1.12;
  return `<svg class="bot still" viewBox="${x + w / 2 - s / 2} ${y + h / 2 - s / 2} ${s} ${s}" aria-hidden="true">${p.svg}</svg>`;
}

// The bay: 1000x900, robot stands on the platform. equip null draws the room alone.
// fit 'slice' fills its box and crops the room; 'meet' shows the whole room (seams and lamp rod run on above it).
function sceneSVG(baseId, equip, cls = '', fit = 'slice') {
  const tier = PART[baseId]?.tier || 1;
  return `<svg class="scene" viewBox="0 0 1000 900" preserveAspectRatio="xMidYMax ${fit}" aria-hidden="true">
    <path class="seam" d="M180-900v1600M820-900v1600"/>
    ${tier >= 4 ? `
      <path class="wire" d="M640 104Q820 190 1000 100"/>
      ${[[730, 136, 'br'], [820, 146, 'cr'], [910, 134, 'mt']].map(([x, y, c], i) => `<circle class="${c} pulse" style="animation-delay:${-i * 0.5}s" cx="${x}" cy="${y + 9}" r="9"/>`).join('')}` : ''}
    ${tier >= 3 ? `
      <circle class="zn" cx="800" cy="290" r="118"/><circle class="space" cx="800" cy="290" r="102"/>
      <path class="stars" d="M742 236h.1M772 330h.1M846 222h.1M758 284h.1M870 352h.1M812 250h.1M728 318h.1"/>
      <circle class="br" cx="832" cy="306" r="38"/><ellipse class="pring" cx="832" cy="306" rx="64" ry="13" transform="rotate(-18 832 306)"/>` : ''}
    ${tier >= 2 ? `
      <path class="rod" d="M640-900v1032"/><path class="zn" d="M590 172l20-40h60l20 40z"/><path class="cone" d="M590 172L250 760h500L690 172z"/>
      <rect class="panel" x="70" y="200" width="210" height="250" rx="14"/>
      <path class="tool" d="M120 250v120M112 250h16M170 250v90l22 22M235 248a16 16 0 1 0 0 34v80"/>
      <rect class="zn" x="730" y="500" width="220" height="10" rx="5"/>
      <rect class="panel" x="746" y="436" width="72" height="64" rx="8"/><rect class="panel" x="828" y="456" width="52" height="44" rx="8"/>
      <rect class="jar" x="892" y="446" width="44" height="54" rx="10"/><path class="jarfill" d="M902 486h.1M914 490h.1M926 486h.1M908 476h.1M920 478h.1"/>` : ''}
    <rect class="floor" x="0" y="700" width="1000" height="200"/><rect class="skirt" x="0" y="692" width="1000" height="8"/>
    ${tier >= 4 ? `
      <path class="stars" d="M60 760h.1M150 850h.1M250 726h.1M330 876h.1M700 860h.1M790 730h.1M880 836h.1M950 770h.1M560 884h.1M430 716h.1"/>
      <g class="orb"><ellipse cx="500" cy="772" rx="340" ry="76"/><ellipse cx="500" cy="772" rx="440" ry="100"/></g>
      <circle class="br" cx="160" cy="772" r="8"/><circle class="mt" cx="940" cy="772" r="8"/>` : ''}
    <rect class="zn" x="86" y="520" width="48" height="62" rx="10"/><circle class="ey pulse" cx="110" cy="540" r="5"/>
    <path class="cable" d="M262 772c-100 12-142-28-152-120v-70"/>
    ${tier >= 3 ? `
      <path class="stem" d="M860 700v-60"/><path class="leaf" d="M860 660c-30-4-42-26-40-44 26 4 40 22 40 44z"/><path class="leaf" d="M860 644c28 0 44-18 44-38-26 0-42 16-44 38z"/>
      <path class="pot" d="M830 696h60l-8 62h-44z"/>` : ''}
    <ellipse class="plat-side" cx="500" cy="780" rx="250" ry="52"/><ellipse class="plat-top" cx="500" cy="760" rx="250" ry="52"/>
    <ellipse class="plat-ring" cx="500" cy="760" rx="196" ry="40"/>
    ${equip ? robotSVG(equip, 'x="250" y="165" width="500" height="650"', cls) : ''}
  </svg>`;
}
