# Regenerates .impeccable/design.json from DESIGN.md (front matter tokens + named rules, dos and don'ts).
import json, re, colorsys, datetime
root = 'C:/NutsAndBolts/'
md = open(root + 'DESIGN.md', encoding='utf-8').read()
front = md.split('---')[1]
colors = dict(re.findall(r'^  ([a-z-]+): "(#[0-9A-Fa-f]{6}|rgba\([^)]*\))"', front.split('typography:')[0], re.M))

ROLE = {
    'ready-green': ('primary', 'Ready Green'), 'on-ready': ('primary', 'On-Ready'),
    'cool-steel': ('secondary', 'Cool Steel'), 'joint': ('secondary', 'Joint'),
    'uranium-glow': ('tertiary', 'Uranium Glow'), 'nut-gold': ('tertiary', 'Nut Gold'), 'shear-coral': ('tertiary', 'Shear Coral'),
}
def ramp(h):
    r, g, b = (int(h[i:i + 2], 16) / 255 for i in (1, 3, 5))
    hh, l, s = colorsys.rgb_to_hls(r, g, b)
    out = []
    for L in (0.12, 0.22, 0.32, 0.42, 0.55, 0.68, 0.8, 0.92):
        rr, gg, bb = colorsys.hls_to_rgb(hh, L, s)
        out.append('#%02X%02X%02X' % (round(rr * 255), round(gg * 255), round(bb * 255)))
    return out
colorMeta = {}
for k, v in colors.items():
    role, name = ROLE.get(k, ('neutral', k.replace('-', ' ').title()))
    e = {'role': role, 'displayName': name, 'canonical': v}
    if v.startswith('#'): e['tonalRamp'] = ramp(v)
    colorMeta[k] = e

typo = {
    'display': 'Screen titles in the header.', 'headline': 'The empty-state line; the picked habit in the detail pane.',
    'title-large': "A shop part's name in its detail.", 'title': 'Habit names on cards and in history; the steppers.',
    'body-large': 'Header subtitle, notes, part names, field text.', 'body': 'Habit metadata, summaries, helper and reward text.',
    'label-large': 'Buttons, segments, shop tabs, day numbers, attribute names.', 'label-medium': 'Attribute meanings, stat captions.',
    'label': 'Tracked uppercase group labels, weekday heads, the nameplate.', 'numeral': 'Balances, levels, counters and stats, tabular.',
}
typographyMeta = {k: {'displayName': k.replace('-', ' ').title(), 'purpose': v} for k, v in typo.items()}

def rules(section_title):
    sec = md.split('## ' + section_title, 1)[1].split('\n## ', 1)[0]
    return [(n, b.strip()) for n, b in re.findall(r'\*\*(The [^*]+?)\.\*\* (.+)', sec)]
named = [{'name': n, 'body': b, 'section': s.lower()} for s in ('Colors', 'Typography', 'Layout') for n, b in rules(s)]
def bullets(head):
    sec = md.split('### ' + head, 1)[1].split('\n###', 1)[0].split('\n## ', 1)[0]
    return [re.sub(r'\*\*', '', l[2:]).strip() for l in sec.splitlines() if l.startswith('- ')]
overview = md.split('## Overview', 1)[1].split('**Key Characteristics:**')[0]
overview = re.sub(r'\*\*Creative North Star: "[^"]+"\*\*', '', overview).strip()

C = colors
def css(cls, body): return '.%s { font-family: Lexend, sans-serif; %s }' % (cls, body)
components = [
    {'name': 'Done Button', 'kind': 'button', 'refersTo': 'button-done', 'description': 'The one clear primary action per habit; also Buy, Save, Add.',
     'html': '<button class="ds-btn-done">Done</button>',
     'css': css('ds-btn-done', 'background: %s; color: %s; height: 44px; padding: 0 24px; border: none; border-radius: 9999px; font-size: 16px; font-weight: 600;' % (C['ready-green'], C['on-ready'])) +
            ' .ds-btn-done:disabled { background: %s; color: %s; }' % (C['raised'], C['dim'])},
    {'name': 'Menu Button', 'kind': 'button', 'refersTo': 'button-menu', 'description': "A 44dp outlined circle beside Done; its menu holds Skip, Fail, take back, Edit and Delete.",
     'html': '<button class="ds-btn-menu" aria-label="More">⋮</button>',
     'css': css('ds-btn-menu', 'width: 44px; height: 44px; border-radius: 50%%; background: transparent; border: 1px solid %s; color: %s;' % (C['edge'], C['cool-steel']))},
    {'name': 'Card', 'kind': 'container', 'refersTo': 'card', 'description': 'The container for habits, form groups, history items and calendars.',
     'html': '<div class="ds-card">…</div>',
     'css': css('ds-card', 'background: %s; border: 1px solid %s; border-radius: 12px; padding: 16px; color: %s;' % (C['card'], C['border'], C['frost']))},
    {'name': 'Segments', 'kind': 'control', 'refersTo': 'segment-active', 'description': 'A pill of choices whose selection slides on a spring.',
     'html': '<div class="ds-seg"><span class="on">Build it</span><span>Break it</span></div>',
     'css': css('ds-seg', 'display: flex; border: 1px solid %s; border-radius: 22px; padding: 3px;' % C['edge']) +
            ' .ds-seg span { flex: 1; text-align: center; padding: 10px 8px; color: %s; border-radius: 20px; } .ds-seg .on { background: %s; color: %s; box-shadow: inset 0 0 0 1.5px %s; }' % (C['dim'], C['raised'], C['frost'], C['cool-steel'])},
    {'name': 'Field', 'kind': 'input', 'refersTo': 'field', 'description': 'A faint fill and a ready-green outline when focused.',
     'html': '<input class="ds-field" placeholder="Read 20 minutes">',
     'css': css('ds-field', 'background: rgba(255,255,255,0.05); border: 1px solid %s; border-radius: 10px; padding: 12px 16px; color: %s;' % (C['border'], C['frost'])) +
            ' .ds-field:focus { outline: none; border-color: %s; }' % C['ready-green']},
    {'name': 'Bottom Bar', 'kind': 'navigation', 'refersTo': 'nav-container', 'description': 'Lab, Shop, Habits, Diary at the foot of every window; never a side rail.',
     'html': '<nav class="ds-bar"><a class="on">Lab</a><a>Shop</a><a>Habits</a><a>Diary</a></nav>',
     'css': css('ds-bar', 'display: flex; background: %s; height: 60px;' % C['rail']) +
            ' .ds-bar a { flex: 1; text-align: center; color: %s; font-size: 13px; } .ds-bar .on { color: %s; }' % (C['dim'], C['frost'])},
    {'name': 'Bolt Slots', 'kind': 'indicator', 'refersTo': 'bolt-slots', 'description': 'Driven head, dashed ring, sheared head, open hole: each slot state its own form.',
     'html': '<span class="ds-bolt done"></span><span class="ds-bolt skip"></span><span class="ds-bolt fail"></span><span class="ds-bolt"></span>',
     'css': '.ds-bolt { display: inline-block; width: 20px; height: 20px; border-radius: 50%%; border: 2px solid %s; background: %s; margin: 3px; } .ds-bolt.done { background: %s; border-color: %s; } .ds-bolt.skip { background: transparent; border-style: dashed; } .ds-bolt.fail { background: %s; border-color: %s; }' % (C['dim'], C['rail'], C['cool-steel'], C['joint'], C['shear-coral'], C['shear-coral'])},
    {'name': 'Gauge', 'kind': 'indicator', 'refersTo': 'gauge', 'description': 'A glow-to-steel fill on an edge track with a soft halo and a bright leading spark.',
     'html': '<div class="ds-gauge"><i style="width:60%"></i></div>',
     'css': '.ds-gauge { height: 8px; border-radius: 4px; background: %s; } .ds-gauge i { display: block; height: 100%%; border-radius: 4px; background: linear-gradient(90deg, %s, %s); box-shadow: 0 0 10px %s66; }' % (C['edge'], C['uranium-glow'], C['cool-steel'], C['uranium-glow'])},
    {'name': 'Snackbar', 'kind': 'feedback', 'refersTo': 'snackbar', 'description': 'After every log and purchase, with Undo.',
     'html': '<div class="ds-snack">+5 bolts <button>Undo</button></div>',
     'css': css('ds-snack', 'background: %s; color: %s; border-radius: 4px; padding: 14px 16px; display: flex; justify-content: space-between;' % (C['raised'], C['frost'])) +
            ' .ds-snack button { background: none; border: none; color: %s; font-weight: 600; }' % C['ready-green']},
]

out = {
    'schemaVersion': 2,
    'generatedAt': datetime.datetime.now(datetime.timezone.utc).strftime('%Y-%m-%dT%H:%M:%SZ'),
    'title': 'Design System: Nuts And Bolts',
    'extensions': {
        'colorMeta': colorMeta,
        'typographyMeta': typographyMeta,
        'shadows': [
            {'name': 'uranium-glow', 'value': 'radial-gradient(circle 420px at center, rgba(166,255,77,0.08..0.26), transparent)', 'purpose': "The reactor's wash over the bay; with the gauges' and a critical frame's halo, the only glow."},
            {'name': 'material-default', 'value': 'Material 3 default elevation', 'purpose': 'FAB and snackbar only; nothing else casts a shadow.'},
        ],
        'motion': [
            {'name': 'bolt-drive', 'value': '700ms cubic ease-out', 'purpose': 'Done screws the newest bolt in: the head grows while its cross turns three quarters.'},
            {'name': 'segment-slide', 'value': 'spring(dampingRatio 0.8, stiffness 500)', 'purpose': 'A segmented choice slides its selection.'},
            {'name': 'positron', 'value': '3000ms loop', 'purpose': 'Sparks rising off a critical frame.'},
            {'name': 'frame-clock', 'value': 'one frame clock, frozen when animator duration scale is 0', 'purpose': 'Drives the robot, the reactor, the drive and the positrons.'},
        ],
        'breakpoints': [
            {'name': 'wide', 'value': 'landscape and width >= 600dp (bay beside a side pane)'},
            {'name': 'phone', 'value': 'short side < 600dp'},
            {'name': 'split', 'value': 'width >= 720dp (Habits day view and form in two columns)'},
            {'name': 'card-stack', 'value': 'habit card < 480dp (counter and buttons on their own lines)'},
        ],
    },
    'components': components,
    'narrative': {
        'northStar': re.search(r'Creative North Star: "([^"]+)"', md).group(1),
        'overview': overview,
        'keyCharacteristics': [re.sub(r'^- ', '', l) for l in md.split('**Key Characteristics:**')[1].split('\n## ')[0].strip().splitlines() if l.startswith('- ')],
        'rules': named,
        'dos': bullets('Do:'),
        'donts': bullets("Don't:"),
    },
}
json.dump(out, open(root + '.impeccable/design.json', 'w', encoding='utf-8'), indent=2, ensure_ascii=False)
print('ok', len(colorMeta), len(named), len(out['narrative']['dos']))
