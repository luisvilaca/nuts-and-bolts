'use strict';
// Robot parts and the bay scene, all drawn as SVG strings.
// Robot space is 400x520 with the feet landing at y=468. Arms and legs are drawn once and mirrored.

const mirror = s => `<g transform="translate(400 0) scale(-1 1)">${s}</g>`;
const both = s => s + mirror(s);
const arms = s => `<g class="arm arm-l">${s}</g><g class="arm arm-r">${mirror(s)}</g>`;

const PARTS = [
  // heads
  { id: 'head-box', slot: 'head', name: 'Box Mk I', no: 'NB-H01', box: [96, 70, 208, 152], svg: `
    <circle class="zn" cx="113" cy="152" r="10"/><circle class="zn" cx="287" cy="152" r="10"/>
    <rect class="sh" x="116" y="94" width="168" height="116" rx="30"/>
    <rect class="gl" x="132" y="116" width="136" height="68" rx="24"/>
    <path class="hl" d="M150 130q14-6 34-6"/>
    <g class="eyes"><rect class="ey" x="161" y="134" width="22" height="30" rx="11"/><rect class="ey" x="217" y="134" width="22" height="30" rx="11"/></g>` },
  { id: 'head-dome', slot: 'head', name: 'Dome', no: 'NB-H02', bolts: 120, box: [96, 70, 208, 152], svg: `
    <path class="sh" d="M110 186C110 118 150 82 200 82s90 36 90 104c0 16-12 24-28 24H138c-16 0-28-8-28-24z"/>
    <path class="gl" d="M130 160c0-26 30-38 70-38s70 12 70 38c0 22-30 32-70 32s-70-10-70-32z"/>
    <path class="hl" d="M152 138q20-9 48-9"/>
    <g class="eyes"><ellipse class="ey" cx="172" cy="159" rx="15" ry="11" transform="rotate(-10 172 159)"/><ellipse class="ey" cx="228" cy="159" rx="15" ry="11" transform="rotate(10 228 159)"/></g>` },
  { id: 'head-binoc', slot: 'head', name: 'Twin Lens', no: 'NB-H03', bolts: 200, box: [96, 70, 208, 152], svg: `
    <rect class="zn" x="180" y="170" width="40" height="42" rx="8"/><rect class="zn" x="182" y="130" width="36" height="26" rx="6"/>
    <rect class="sh" x="106" y="102" width="90" height="84" rx="36" transform="rotate(-6 151 144)"/>
    <rect class="sh" x="204" y="102" width="90" height="84" rx="36" transform="rotate(6 249 144)"/>
    <circle class="gl" cx="152" cy="144" r="30"/><circle class="gl" cx="248" cy="144" r="30"/>
    <g class="eyes"><circle class="ey" cx="156" cy="147" r="13"/><circle class="ey" cx="244" cy="147" r="13"/></g>` },
  { id: 'head-hex', slot: 'head', name: 'Hex Head', no: 'NB-H04', bolts: 150, nuts: 3, box: [96, 70, 208, 152], svg: `
    <path class="sh shr" d="M130 150l35-60h70l35 60-35 60h-70z"/>
    <circle class="gl" cx="200" cy="150" r="45"/>
    <path class="hl" d="M172 128q12-10 28-11"/>
    <g class="eyes"><circle class="ey" cx="182" cy="152" r="11"/><circle class="ey" cx="218" cy="152" r="11"/></g>` },

  // bodies
  { id: 'body-crate', slot: 'body', name: 'Crate', no: 'NB-B01', box: [106, 222, 188, 158], svg: `
    <rect class="sh" x="126" y="232" width="148" height="138" rx="28"/>
    <rect class="sh2" x="148" y="256" width="104" height="54" rx="12"/>
    <circle class="ey pulse" cx="170" cy="283" r="8"/>
    <path class="ln" d="M194 272h42M194 283h42M194 294h28M152 336h96"/>` },
  { id: 'body-capsule', slot: 'body', name: 'Capsule', no: 'NB-B02', bolts: 140, box: [106, 222, 188, 158], svg: `
    <path class="sh" d="M130 262c0-22 22-30 70-30s70 8 70 30l-6 66c-4 28-26 44-64 44s-60-16-64-44z"/>
    <circle class="gl" cx="200" cy="288" r="28"/>
    <circle class="ring spin" cx="200" cy="288" r="17"/>` },
  { id: 'body-reactor', slot: 'body', name: 'Reactor', no: 'NB-B03', bolts: 180, nuts: 4, box: [106, 222, 188, 158], svg: `
    <path class="sh" d="M116 248q0-16 16-16h136q16 0 16 16l-12 106q-2 16-18 16H146q-16 0-18-16z"/>
    <circle class="br" cx="200" cy="294" r="33"/><circle class="gl" cx="200" cy="294" r="25"/>
    <circle class="ey pulse" cx="200" cy="294" r="12"/>
    <path class="ln" d="M134 258h22M135 271h20M244 258h22M245 271h20"/>` },

  // arms (left arm; shoulder at 120,264)
  { id: 'arms-clamp', slot: 'arms', name: 'Clamps', no: 'NB-A01', box: [70, 240, 260, 130], svg: arms(`
    <circle class="zn" cx="120" cy="264" r="14"/>
    <rect class="sh" x="90" y="256" width="30" height="72" rx="15"/>
    <path class="claw" d="M96 330q-8 18 6 28M114 330q8 18-6 28"/>`) },
  { id: 'arms-flex', slot: 'arms', name: 'Flex Hose', no: 'NB-A02', bolts: 80, box: [70, 240, 260, 130], svg: arms(`
    <circle class="zn" cx="120" cy="264" r="14"/>
    <rect class="sh2" x="92" y="258" width="26" height="18" rx="9"/><rect class="sh" x="92" y="279" width="26" height="18" rx="9"/>
    <rect class="sh2" x="92" y="300" width="26" height="18" rx="9"/><rect class="sh" x="92" y="321" width="26" height="18" rx="9"/>
    <circle class="sh" cx="105" cy="354" r="15"/>`) },
  { id: 'arms-mitt', slot: 'arms', name: 'Mitts', no: 'NB-A03', bolts: 110, box: [70, 240, 260, 130], svg: arms(`
    <circle class="zn" cx="120" cy="264" r="12"/>
    <rect class="zn" x="100" y="262" width="11" height="60" rx="5"/>
    <circle class="sh" cx="124" cy="330" r="9"/><ellipse class="sh" cx="105" cy="340" rx="21" ry="24"/>`) },
  { id: 'arms-fin', slot: 'arms', name: 'Fins', no: 'NB-A04', bolts: 100, nuts: 2, box: [70, 240, 260, 130], svg: arms(`
    <path class="sh" d="M112 248c-26 20-32 76-14 114 18-24 26-74 22-112z"/>`) },

  // legs (left leg; hips at 168 and 232)
  { id: 'legs-stub', slot: 'legs', name: 'Stubs', no: 'NB-L01', box: [100, 350, 200, 126], svg: both(`
    <rect class="zn" x="158" y="356" width="22" height="84" rx="8"/>
    <rect class="sh" x="140" y="432" width="56" height="36" rx="18"/>`) },
  { id: 'legs-spring', slot: 'legs', name: 'Springs', no: 'NB-L02', bolts: 90, box: [100, 350, 200, 126], svg: both(`
    <path class="spring" d="M169 362l-12 10 24 12-24 12 24 12-24 12 12 10"/>
    <ellipse class="sh" cx="169" cy="452" rx="28" ry="16"/>`) },
  { id: 'legs-tread', slot: 'legs', name: 'Treads', no: 'NB-L03', bolts: 160, box: [100, 350, 200, 126], svg: `
    <rect class="zn" x="146" y="358" width="108" height="34" rx="14"/>` + both(`
    <rect class="gl" x="108" y="388" width="78" height="80" rx="34"/><rect class="tread" x="108" y="388" width="78" height="80" rx="34"/>
    <circle class="sh" cx="147" cy="412" r="11"/><circle class="sh" cx="147" cy="446" r="11"/>`) },
  { id: 'legs-hover', slot: 'legs', name: 'Hover Jet', no: 'NB-L04', bolts: 220, nuts: 5, box: [100, 350, 200, 126], svg: `
    <path class="zn" d="M166 364h68l-14 34h-40z"/>
    <ellipse class="ey pulse" cx="200" cy="410" rx="22" ry="9"/>
    <ellipse class="ring pulse" cx="200" cy="452" rx="62" ry="12"/>` },

  // accessories; `at` says which layer they ride on
  { id: 'acc-antenna', slot: 'acc', at: 'head-back', name: 'Antenna', no: 'NB-X01', bolts: 40, box: [190, 30, 110, 100], svg: `
    <path class="rod" d="M236 112l16-56"/><circle class="ey pulse" cx="253" cy="50" r="9"/>` },
  { id: 'acc-scarf', slot: 'acc', at: 'front', name: 'Scarf', no: 'NB-X02', bolts: 50, box: [140, 208, 120, 94], svg: `
    <path class="scarf" d="M168 240l-14 50 26-8 6-38z"/>
    <path class="scarf" d="M158 218q42 24 84 0l4 18q-46 26-92 0z"/>` },
  { id: 'acc-sprout', slot: 'acc', at: 'head-back', name: 'Sprout', no: 'NB-X03', bolts: 60, box: [140, 20, 120, 128], svg: `
    <path class="stem" d="M200 140V62"/>
    <path class="leaf" d="M200 72c-24-2-34-18-32-30 20 2 32 14 32 30z"/><path class="leaf" d="M200 62c22 0 34-14 34-28-20 0-32 12-34 28z"/>` },
  { id: 'acc-pack', slot: 'acc', at: 'body-back', name: 'Jet Pack', no: 'NB-X04', bolts: 150, nuts: 2, box: [126, 196, 148, 76], svg: `
    <rect class="zn" x="136" y="206" width="42" height="56" rx="14"/><rect class="zn" x="222" y="206" width="42" height="56" rx="14"/>
    <rect class="br" x="141" y="206" width="32" height="12" rx="6"/><rect class="br" x="227" y="206" width="32" height="12" rx="6"/>` },
  { id: 'acc-halo', slot: 'acc', at: 'head-front', name: 'Halo', no: 'NB-X05', nuts: 6, box: [140, 36, 120, 54], svg: `
    <ellipse class="halo" cx="200" cy="62" rx="48" ry="11"/>` },

  // base evolutions; each tier keeps everything below it
  { id: 'base-bay', slot: 'base', tier: 1, name: 'Charging Bay', no: 'NB-Z01' },
  { id: 'base-shop', slot: 'base', tier: 2, name: 'Workshop', no: 'NB-Z02', nuts: 4 },
  { id: 'base-dome', slot: 'base', tier: 3, name: 'Observatory', no: 'NB-Z03', nuts: 10 },
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
