# -*- coding: utf-8 -*-
import os, re, shutil

SRC = r"D:\Code\MurmolMod\wiki"
STAGE = r"D:\Code\MurmolMod\wiki_flat_stage"

# old link ref -> flat page name
M = {}
for x in ["Alfar-Shrine","Astral-Block","Astral-Lantern","Astral-Ore","Astral-Sapling","Astral-Stone","Astral-Torch","Astral-Wood-Set","Banza-Pedestal","Cage","Cursed-Stone","Ice-Flower-Wild","Ice-Sharp-Ore","Magic-Crystal-Cluster","Mango-Bush","Spirit-Table","Two-Principles"]:
    M["Blocks/"+x]=x
M["Blocks/Truth-Mirror"]="Truth-Mirror-Block"

for x in ["Alfar","Astral-Drake","Gluttony-Slime","Murmol-NPC","Pepper-CH","Projectiles"]:
    M["Entities/"+x]=x

for x in ["Chen-Huang","Ferocious","Human","Komainu","Moss-Beast","Petalfall-Vernal","Silkmoth","Wenyao"]:
    M["Forms/"+x]=x

items=["Alfar-Spit","Ancient-Knowledge-Scrap","Astral-Armor","Astral-Codex","Astral-Core","Astral-Ingot","Astral-Matrix","Astral-Ore-Shard","Astral-Tools","Blooming-Blade","Fire-Blade-Claw","Fragrant-Corsage","Frost-Armor","Frost-Ingot","Frost-Tools","Ice-And-Fire-Sword","Ice-Bolt-Item","Ice-Fish","Ice-Flower","Ice-Sharp","Ice-Wand","Magic-Crystal","Magic-Shard","Mango","Mango-Bush-Stages","Maocrys-Blessing","Murmol","Murmol-Soul","Music-Discs","Petal","Soul-Bottle","Soul-Items","Spawn-Eggs","The-Astral-Tome","Tree-Heart","Truth-Mirror","Unknown-Badge"]
for x in items:
    M["Items/"+x]=x

M["Systems/Cursed-Stone"]="Cursed-Stone"
M["Systems/Two-Principles"]="Two-Principles-System"
for x in ["Astral-Biome","Astral-Core-Impact","Astral-Infection","Cage-System","Effects-Potions","Enchantments","JEI-Integration","Localization","Mahjong","Petrify","Reveal","Transformation"]:
    M["Systems/"+x]=x

M["Guide/Getting-Started"]="Getting-Started"
M["Content-Index"]="Home"

# also map bare refs to same flat names (safety)
bare_flat={v:v for v in set(M.values())}
bare_flat.update(M)

LINK=re.compile(r'\]\(([^()#)]+)\)')

def rewrite(text):
    def repl(m):
        ref=m.group(1).strip()
        if ref.startswith(('http:','https:','mailto:','#','<','.')):
            return m.group(0)
        anchor=''
        clean=ref
        if '#' in clean:
            clean,anchor=clean.split('#',1)
            anchor='#'+anchor
        clean=clean.strip()
        target=bare_flat.get(clean)
        if target:
            return ']('+target+anchor+')'
        return m.group(0)
    return LINK.sub(repl,text)

if os.path.exists(STAGE):
    shutil.rmtree(STAGE)
os.makedirs(STAGE)

# collect all md files under src
files=[]
for root,dirs,fs in os.walk(SRC):
    for f in fs:
        if f.endswith('.md'):
            files.append(os.path.join(root,f))

# write pages (flat)
for path in files:
    rel=os.path.relpath(path,SRC).replace('\\','/')
    base=os.path.basename(path)
    with open(path,encoding='utf-8') as fh:
        text=fh.read()
    newtext=rewrite(text)
    if rel in ('Content-Index.md','Home.md'):
        continue  # handled separately
    if rel == 'Systems/Cursed-Stone.md':
        continue  # merged into block page Cursed-Stone.md
    # map rel (e.g. Blocks/Cage.md) to flat name
    if '/' in rel:
        cat=rel.split('/')[0]
        key=cat+'/'+base[:-3]
        flat=M.get(key)
        if not flat:
            raise RuntimeError('no mapping for '+key)
    else:
        # _Sidebar/_Footer stay
        flat=base[:-3]
    out=os.path.join(STAGE,flat+'.md')
    with open(out,'w',encoding='utf-8',newline='\n') as fh:
        fh.write(newtext)

print("pages written:", len([f for f in os.listdir(STAGE) if f.endswith('.md')]))
