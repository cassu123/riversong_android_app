/* global React */
// All screen components for the River Song Android app prototype.

const { useState, useEffect, useRef } = React;

const Icon = ({ name, fill = false, sm = false, lg = false, style = {} }) => (
  <span className={`material-symbols-rounded${fill?' fill':''}${sm?' sm':''}${lg?' lg':''}`} style={style}>{name}</span>
);

// ─────────────────────────────────────────────────────────────
// SPEAK — voice surface with holographic bust + audio ring
// ─────────────────────────────────────────────────────────────
function SpeakScreen({ voiceState, setVoiceState, transcript }) {
  const stateLabels = {
    idle: 'IDLE — TAP TO SPEAK',
    connecting: 'CONNECTING…',
    listening: 'LISTENING',
    transcribing: 'TRANSCRIBING',
    thinking: 'THINKING',
    speaking: 'SPEAKING',
    error: 'ERROR',
  };
  const onTap = () => {
    if (voiceState === 'idle') setVoiceState('listening');
    else if (voiceState === 'listening') setVoiceState('thinking');
    else setVoiceState('idle');
  };
  return (
    <div className="rs-page" style={{ display: 'flex', flexDirection: 'column', height: '100%', padding: '8px 16px 16px' }}>
      {/* status pill */}
      <div style={{ display: 'flex', justifyContent: 'center', marginTop: 4 }}>
        <div className="rs-micro" style={{
          display: 'inline-flex', alignItems: 'center', gap: 8,
          padding: '6px 14px', borderRadius: 9999,
          background: 'var(--rs-surface-c)', color: 'var(--rs-on-surface-var)',
          border: '1px solid var(--rs-outline-variant)',
        }}>
          <span className="rs-dot" style={{
            background: voiceState==='listening'?'var(--rs-tertiary)'
              :voiceState==='speaking'?'#82E1B7'
              :voiceState==='thinking'?'#C4AAFF'
              :voiceState==='error'?'var(--rs-error)'
              :'var(--rs-primary)',
            boxShadow: voiceState==='idle'?'none':'0 0 8px currentColor',
          }} />
          {stateLabels[voiceState]}
        </div>
      </div>

      {/* bust */}
      <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', position: 'relative' }}>
        <div style={{ position: 'relative', width: 280, height: 280, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <window.AudioRing state={voiceState} size={280} />
          <div style={{ position: 'relative', zIndex: 1 }}>
            <window.HoloBust state={voiceState} size={200} audioLevel={voiceState==='speaking'?0.6:0} />
          </div>
        </div>
      </div>

      {/* transcript area */}
      <div className="rs-card outlined" style={{ minHeight: 78, padding: 14, marginBottom: 18 }}>
        <div className="rs-role" style={{ color: 'var(--rs-primary)', marginBottom: 6 }}>RIVER SONG</div>
        <div className="rs-body" style={{ color: 'var(--rs-on-surface)' }}>
          {voiceState === 'idle' && <span style={{ color: 'var(--rs-on-surface-var)' }}>Conversation will appear here.</span>}
          {voiceState === 'listening' && <span style={{ color: 'var(--rs-on-surface-var)' }}>Go ahead, I'm listening<span className="rs-cursor" /></span>}
          {voiceState === 'transcribing' && <span style={{ color: 'var(--rs-on-surface-var)' }}>"{transcript}"</span>}
          {voiceState === 'thinking' && <span style={{ color: 'var(--rs-on-surface-var)' }}>Working on it<span className="rs-cursor" /></span>}
          {voiceState === 'speaking' && <span>The garage door has been closed. Front porch lights are on until 11:30 PM.<span className="rs-cursor" /></span>}
          {voiceState === 'error' && <span style={{ color: 'var(--rs-error)' }}>Microphone access denied. Allow mic access and try again.</span>}
        </div>
      </div>

      {/* mic button row */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 12 }}>
        <button className="rs-btn icon-only outlined" aria-label="Reset" onClick={()=>setVoiceState('idle')}>
          <Icon name="restart_alt" sm />
        </button>
        <button
          onClick={onTap}
          className={voiceState==='listening'?'rs-listening':''}
          style={{
            width: 88, height: 88, borderRadius: 9999, border: 0,
            background: voiceState==='listening' ? 'var(--rs-tertiary-c)' : 'var(--rs-primary-c)',
            color: voiceState==='listening' ? 'var(--rs-on-tertiary-c)' : 'var(--rs-on-primary-c)',
            boxShadow: 'var(--rs-elev-3)', cursor: 'pointer',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}
        >
          <Icon name={voiceState==='listening'?'stop':'mic'} fill style={{ fontSize: 36 }} />
        </button>
        <button className="rs-btn icon-only outlined" aria-label="Keyboard">
          <Icon name="keyboard" sm />
        </button>
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// CHAT — conversation transcript
// ─────────────────────────────────────────────────────────────
const CHAT_MODELS = [
  { id: 'llama3-8b', label: 'Llama 3 · 8B', sub: 'Local · Ollama · GPU', host: 'local', badge: 'GPU', tone: 'Fast, default' },
  { id: 'llama3-70b', label: 'Llama 3 · 70B', sub: 'Local · Ollama · CPU', host: 'local', badge: 'CPU', tone: 'Deep, slow' },
  { id: 'mixtral', label: 'Mixtral 8x7B', sub: 'Local · Ollama', host: 'local', badge: 'GPU', tone: 'Reasoning' },
  { id: 'phi3', label: 'Phi-3 mini', sub: 'Local · Ollama', host: 'local', badge: 'CPU', tone: 'Snappy' },
  { id: 'qwen', label: 'Qwen 2.5 · 14B', sub: 'Local · Ollama', host: 'local', badge: 'GPU', tone: 'Multilingual' },
  { id: 'claude', label: 'Claude Haiku 4.5', sub: 'Cloud · Anthropic', host: 'cloud', badge: 'COST', tone: 'Sharp' },
  { id: 'gpt4o', label: 'GPT-4o mini', sub: 'Cloud · OpenAI', host: 'cloud', badge: 'COST', tone: 'General' },
  { id: 'auto', label: 'Auto-route', sub: 'River chooses by task', host: 'auto', badge: 'SPEAK', tone: 'Smart' },
];

function ModelPicker({ open, onClose, model, setModel }) {
  if (!open) return null;
  return (
    <>
      <div onClick={onClose} style={{ position: 'absolute', inset: 0, background: 'var(--rs-scrim)', zIndex: 20 }} />
      <div style={{
        position: 'absolute', left: 12, right: 12, top: 12,
        background: 'var(--rs-surface-low)',
        borderRadius: 'var(--rs-r-lg)', boxShadow: 'var(--rs-elev-3)',
        border: '1px solid var(--rs-outline-variant)',
        zIndex: 21, overflow: 'hidden',
        animation: 'rs-page-in 200ms',
        maxHeight: '70%', display: 'flex', flexDirection: 'column',
      }}>
        <div style={{ padding: '14px 16px', borderBottom: '1px solid var(--rs-outline-variant)', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div className="rs-section-title">Choose model</div>
          <button className="rs-btn icon-only" style={{ background: 'transparent', color: 'var(--rs-on-surface-var)', height: 28, width: 28 }} onClick={onClose}><Icon name="close" sm /></button>
        </div>
        <div style={{ overflow: 'auto' }}>
          {CHAT_MODELS.map(m => {
            const active = m.id === model.id;
            return (
              <div key={m.id} onClick={() => { setModel(m); onClose(); }} style={{
                display: 'flex', alignItems: 'center', gap: 12, padding: '12px 16px',
                cursor: 'pointer',
                background: active ? 'color-mix(in srgb, var(--rs-primary) 10%, transparent)' : 'transparent',
                borderLeft: active ? '3px solid var(--rs-primary)' : '3px solid transparent',
              }}>
                <div style={{
                  width: 32, height: 32, borderRadius: 9999,
                  background: m.host==='cloud' ? 'var(--rs-tertiary-c)' : m.host==='auto' ? 'var(--rs-secondary-c)' : 'var(--rs-primary-c)',
                  color: m.host==='cloud' ? 'var(--rs-on-tertiary-c)' : m.host==='auto' ? 'var(--rs-on-secondary-c)' : 'var(--rs-on-primary-c)',
                  display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0,
                }}>
                  <Icon name={m.host==='cloud' ? 'cloud' : m.host==='auto' ? 'auto_awesome' : 'memory'} sm style={{ fontSize: 18 }} />
                </div>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ fontSize: '0.9375rem', fontWeight: 500, display: 'flex', gap: 8, alignItems: 'center' }}>
                    {m.label}
                    {active && <Icon name="check" sm style={{ fontSize: 16, color: 'var(--rs-primary)' }} />}
                  </div>
                  <div className="rs-micro" style={{ color: 'var(--rs-on-surface-var)' }}>{m.sub} · {m.tone}</div>
                </div>
                <span className={`rs-badge ${m.badge.toLowerCase()}`}>{m.badge}</span>
              </div>
            );
          })}
        </div>
      </div>
    </>
  );
}

function ChatScreen() {
  const [input, setInput] = useState('');
  const [model, setModel] = useState(CHAT_MODELS[0]);
  const [pickerOpen, setPickerOpen] = useState(false);
  const messages = [
    { role: 'system', text: 'Session started · 09:14' },
    { role: 'user', text: "What's on the calendar today?" },
    { role: 'assistant', text: 'Three items. Vet appointment for Penny at 10:30, lunch with Sam at 1, and you wanted to be reminded to take the trash out before 6.' },
    { role: 'user', text: 'Push the vet appointment to tomorrow same time.' },
    { role: 'assistant', text: "Done. I moved it to Saturday May 2nd at 10:30. I let Dr. Allen's office know — they confirmed.", badges: ['SPEAK'] },
    { role: 'user', text: 'Anything new from the cameras overnight?' },
    { role: 'assistant', text: 'Two motion events on the back deck around 2:14 AM — looked like the same raccoon twice. Nothing on the front.', badges: ['GPU'] },
  ];
  return (
    <div className="rs-page" style={{ display: 'flex', flexDirection: 'column', height: '100%', position: 'relative' }}>
      {/* model selector bar */}
      <div style={{ padding: '10px 12px 8px', borderBottom: '1px solid var(--rs-outline-variant)' }}>
        <button onClick={() => setPickerOpen(true)} style={{
          width: '100%', display: 'flex', alignItems: 'center', gap: 10,
          padding: '8px 12px', borderRadius: 'var(--rs-r-full)',
          background: 'var(--rs-surface-c)', border: '1px solid var(--rs-outline-variant)',
          color: 'var(--rs-on-surface)', cursor: 'pointer', fontFamily: 'inherit',
        }}>
          <div style={{
            width: 26, height: 26, borderRadius: 9999,
            background: model.host==='cloud' ? 'var(--rs-tertiary-c)' : model.host==='auto' ? 'var(--rs-secondary-c)' : 'var(--rs-primary-c)',
            color: model.host==='cloud' ? 'var(--rs-on-tertiary-c)' : model.host==='auto' ? 'var(--rs-on-secondary-c)' : 'var(--rs-on-primary-c)',
            display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0,
          }}>
            <Icon name={model.host==='cloud' ? 'cloud' : model.host==='auto' ? 'auto_awesome' : 'memory'} sm style={{ fontSize: 16 }} />
          </div>
          <div style={{ flex: 1, textAlign: 'left', minWidth: 0 }}>
            <div style={{ fontSize: '0.875rem', fontWeight: 500, lineHeight: 1.2 }}>{model.label}</div>
            <div className="rs-micro" style={{ color: 'var(--rs-on-surface-var)' }}>{model.sub}</div>
          </div>
          <span className={`rs-badge ${model.badge.toLowerCase()}`}>{model.badge}</span>
          <Icon name="expand_more" sm style={{ color: 'var(--rs-on-surface-var)' }} />
        </button>
      </div>
      <div style={{ flex: 1, padding: '16px 16px 8px', display: 'flex', flexDirection: 'column', gap: 14, overflow: 'auto' }}>
        {messages.map((m, i) => (
          <div key={i} style={{ display: 'flex', flexDirection: 'column', alignItems: m.role==='user'?'flex-end':'flex-start' }}>
            {m.role !== 'system' && (
              <div className="rs-role" style={{ color: 'var(--rs-on-surface-var)', marginBottom: 4, padding: '0 4px' }}>
                {m.role === 'user' ? 'YOU' : 'RIVER SONG'}
              </div>
            )}
            {m.role === 'system' ? (
              <div className="rs-micro" style={{ color: 'var(--rs-on-surface-var)', alignSelf: 'center' }}>{m.text}</div>
            ) : (
              <div style={{
                maxWidth: '88%',
                padding: '10px 14px',
                borderRadius: m.role==='user' ? '20px 20px 4px 20px' : '20px 20px 20px 4px',
                background: m.role==='user' ? 'var(--rs-primary-c)' : 'var(--rs-surface-c)',
                color: m.role==='user' ? 'var(--rs-on-primary-c)' : 'var(--rs-on-surface)',
                fontSize: '0.9375rem', lineHeight: 1.5,
              }}>
                {m.text}
                {m.badges && (
                  <div style={{ display: 'flex', gap: 6, marginTop: 8 }}>
                    {m.badges.map(b => <span key={b} className={`rs-badge ${b.toLowerCase()}`}>{b}</span>)}
                  </div>
                )}
              </div>
            )}
          </div>
        ))}
      </div>
      {/* composer */}
      <div style={{ padding: '8px 12px 14px', display: 'flex', alignItems: 'center', gap: 8, borderTop: '1px solid var(--rs-outline-variant)' }}>
        <button className="rs-btn icon-only" style={{ background: 'var(--rs-surface-c)', color: 'var(--rs-on-surface-var)' }}>
          <Icon name="add" sm />
        </button>
        <div style={{ flex: 1, position: 'relative' }}>
          <input
            className="rs-tf-outlined"
            placeholder="Message River Song"
            value={input}
            onChange={e=>setInput(e.target.value)}
            style={{ borderRadius: 9999, padding: '10px 16px', height: 40, background: 'var(--rs-surface-c)', border: '1px solid var(--rs-outline-variant)', width: '100%', color: 'var(--rs-on-surface)', fontFamily: 'inherit', fontSize: '0.9375rem', outline: 'none' }}
          />
        </div>
        <button className="rs-btn icon-only primary"><Icon name={input?'send':'mic'} sm /></button>
      </div>
      <ModelPicker open={pickerOpen} onClose={()=>setPickerOpen(false)} model={model} setModel={setModel} />
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// MEMORY — what River remembers
// ─────────────────────────────────────────────────────────────
function MemoryScreen() {
  const memories = [
    { kind: 'Person', title: 'Sam', detail: 'Husband. Coffee at 6:45. Allergic to penicillin.', updated: '2d ago' },
    { kind: 'Pet', title: 'Penny', detail: 'Border collie, 4yr. Vet: Dr. Allen, Maple Vet Clinic.', updated: '5h ago' },
    { kind: 'Place', title: 'The garage', detail: 'Door sometimes sticks in summer humidity. Sensor: garage_door_01.', updated: '1w ago' },
    { kind: 'Routine', title: 'Morning', detail: 'Lights on at sunrise -15min. NPR briefing in kitchen.', updated: '3d ago' },
    { kind: 'Preference', title: 'Tone', detail: 'Direct. No hedging. No emoji.', updated: '2w ago' },
  ];
  const [q, setQ] = useState('');
  const filters = ['All', 'People', 'Places', 'Routines', 'Preferences'];
  const [filter, setFilter] = useState('All');
  return (
    <div className="rs-page" style={{ padding: '16px 16px 24px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', marginBottom: 16 }}>
        <div>
          <h1 className="rs-page-title" style={{ margin: 0 }}>Memory</h1>
          <div className="rs-body" style={{ color: 'var(--rs-on-surface-var)', marginTop: 4 }}>5 entries · injected at session start</div>
        </div>
        <button className="rs-btn icon-only" style={{ background: 'var(--rs-primary-c)', color: 'var(--rs-on-primary-c)' }}>
          <Icon name="add" sm />
        </button>
      </div>
      {/* search */}
      <div style={{ position: 'relative', marginBottom: 14 }}>
        <Icon name="search" sm style={{ position: 'absolute', left: 14, top: 11, color: 'var(--rs-on-surface-var)' }} />
        <input className="rs-tf-outlined" placeholder="Search memory" value={q} onChange={e=>setQ(e.target.value)}
          style={{ borderRadius: 9999, height: 40, padding: '0 16px 0 42px', background: 'var(--rs-surface-c)', border: '1px solid var(--rs-outline-variant)', width: '100%', color: 'var(--rs-on-surface)', fontFamily: 'inherit', fontSize: '0.9375rem', outline: 'none' }} />
      </div>
      {/* filter chips */}
      <div style={{ display: 'flex', gap: 8, marginBottom: 16, overflowX: 'auto', paddingBottom: 4 }}>
        {filters.map(f => (
          <button key={f} className={`rs-chip${filter===f?' selected':''}`} onClick={()=>setFilter(f)}>
            {filter===f && <Icon name="check" sm />}
            {f}
          </button>
        ))}
      </div>
      {/* memory cards */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
        {memories.map((m, i) => (
          <div key={i} className="rs-card" style={{ padding: 14 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
              <div className="rs-micro" style={{ color: 'var(--rs-primary)' }}>{m.kind}</div>
              <div className="rs-micro" style={{ color: 'var(--rs-on-surface-var)' }}>{m.updated}</div>
            </div>
            <div style={{ fontSize: '1.0625rem', fontWeight: 500, marginTop: 4, color: 'var(--rs-on-surface)' }}>{m.title}</div>
            <div className="rs-body" style={{ color: 'var(--rs-on-surface-var)', marginTop: 2 }}>{m.detail}</div>
          </div>
        ))}
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// INVENTORY — household items
// ─────────────────────────────────────────────────────────────
function InventoryScreen() {
  const items = [
    { name: 'Coffee beans', loc: 'Pantry', qty: '2 bags', low: false },
    { name: 'Dog food', loc: 'Mudroom', qty: '1/4 bag', low: true },
    { name: 'Paper towels', loc: 'Hall closet', qty: '3 rolls', low: false },
    { name: 'Olive oil', loc: 'Pantry', qty: '1 bottle', low: false },
    { name: 'Trash bags', loc: 'Garage', qty: 'Out', low: true },
    { name: 'Penicillin VK', loc: 'Medicine cab.', qty: 'Sam — out', low: true },
  ];
  return (
    <div className="rs-page" style={{ padding: '16px 16px 24px' }}>
      <h1 className="rs-page-title" style={{ margin: 0 }}>Inventory</h1>
      <div className="rs-body" style={{ color: 'var(--rs-on-surface-var)', marginTop: 4, marginBottom: 16 }}>What's in the house</div>
      {/* summary tiles */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12, marginBottom: 18 }}>
        <div className="rs-card elevated" style={{ padding: 14 }}>
          <div className="rs-micro" style={{ color: 'var(--rs-on-surface-var)' }}>TRACKED</div>
          <div style={{ fontSize: '1.75rem', fontWeight: 400, marginTop: 4 }}>147</div>
        </div>
        <div className="rs-card elevated" style={{ padding: 14 }}>
          <div className="rs-micro" style={{ color: 'var(--rs-warn, #FFB86C)' }}>RUNNING LOW</div>
          <div style={{ fontSize: '1.75rem', fontWeight: 400, marginTop: 4, color: 'var(--rs-warn, #FFB86C)' }}>3</div>
        </div>
      </div>
      <div className="rs-section-title" style={{ marginBottom: 8 }}>Recent</div>
      <div className="rs-card" style={{ padding: 0, overflow: 'hidden' }}>
        {items.map((it, i) => (
          <div key={i} style={{
            display: 'flex', alignItems: 'center', gap: 12,
            padding: '12px 14px',
            borderTop: i===0?'none':'1px solid var(--rs-outline-variant)',
          }}>
            <div style={{
              width: 36, height: 36, borderRadius: 8,
              background: 'var(--rs-surface-high)',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              color: 'var(--rs-on-surface-var)', flexShrink: 0,
            }}>
              <Icon name={it.name.includes('Coffee')?'local_cafe':it.name.includes('Dog')?'pets':it.name.includes('Penicillin')?'medication':'inventory_2'} sm />
            </div>
            <div style={{ flex: 1, minWidth: 0 }}>
              <div style={{ fontSize: '0.9375rem', fontWeight: 500 }}>{it.name}</div>
              <div className="rs-micro" style={{ color: 'var(--rs-on-surface-var)' }}>{it.loc}</div>
            </div>
            <div style={{ textAlign: 'right' }}>
              <div style={{ fontSize: '0.875rem', color: it.low?'var(--rs-warn, #FFB86C)':'var(--rs-on-surface)' }}>{it.qty}</div>
              {it.low && <div className="rs-micro" style={{ color: 'var(--rs-warn, #FFB86C)' }}>LOW</div>}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// MAINTENANCE — house & device upkeep
// ─────────────────────────────────────────────────────────────
function MaintenanceScreen() {
  const tasks = [
    { title: 'Replace HVAC filter', due: 'In 4 days', kind: 'HVAC', overdue: false },
    { title: 'Garage door lubrication', due: 'In 12 days', kind: 'Garage', overdue: false },
    { title: 'Dishwasher rinse aid', due: 'Yesterday', kind: 'Kitchen', overdue: true },
    { title: 'Smoke detector battery — hall', due: '3 weeks', kind: 'Safety', overdue: false },
    { title: 'Gutter clean (back)', due: 'Next month', kind: 'Exterior', overdue: false },
  ];
  return (
    <div className="rs-page" style={{ padding: '16px 16px 24px' }}>
      <h1 className="rs-page-title" style={{ margin: 0 }}>Maintenance</h1>
      <div className="rs-body" style={{ color: 'var(--rs-on-surface-var)', marginTop: 4, marginBottom: 16 }}>1 overdue · 4 upcoming</div>

      <div className="rs-card" style={{ background: 'color-mix(in srgb, var(--rs-error) 14%, var(--rs-surface-c))', borderLeft: '3px solid var(--rs-error)', padding: 14, marginBottom: 16 }}>
        <div className="rs-micro" style={{ color: 'var(--rs-error)' }}>OVERDUE</div>
        <div style={{ fontSize: '1.0625rem', fontWeight: 500, marginTop: 4 }}>Dishwasher rinse aid</div>
        <div className="rs-body" style={{ color: 'var(--rs-on-surface-var)', marginTop: 2 }}>Was due yesterday. I'll add it to the shopping list?</div>
        <div style={{ display: 'flex', gap: 8, marginTop: 12 }}>
          <button className="rs-btn primary" style={{ height: 36, padding: '0 18px' }}>Add to list</button>
          <button className="rs-btn text" style={{ height: 36 }}>Snooze</button>
        </div>
      </div>

      <div className="rs-section-title" style={{ marginBottom: 8 }}>Upcoming</div>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
        {tasks.filter(t=>!t.overdue).map((t, i) => (
          <div key={i} className="rs-card outlined" style={{ padding: 14, display: 'flex', alignItems: 'center', gap: 12 }}>
            <div style={{
              width: 40, height: 40, borderRadius: 12,
              background: 'var(--rs-surface-high)',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              color: 'var(--rs-primary)',
            }}><Icon name="build" sm /></div>
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: '0.9375rem', fontWeight: 500 }}>{t.title}</div>
              <div className="rs-micro" style={{ color: 'var(--rs-on-surface-var)' }}>{t.kind} · {t.due}</div>
            </div>
            <button className="rs-btn icon-only" style={{ background: 'transparent', color: 'var(--rs-on-surface-var)' }}>
              <Icon name="check" sm />
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// STORE — shopping list + auto-reorder
// ─────────────────────────────────────────────────────────────
function StoreScreen() {
  const [items, setItems] = useState([
    { name: 'Dog food (Penny)', store: 'Chewy', auto: true, checked: false },
    { name: 'Trash bags 13gal', store: 'Costco', auto: true, checked: false },
    { name: 'Coffee beans — Counter Culture', store: 'Local', auto: false, checked: false },
    { name: 'Penicillin VK refill', store: 'CVS', auto: false, checked: false },
    { name: 'Paper towels', store: 'Costco', auto: true, checked: true },
  ]);
  const toggle = (i) => setItems(items.map((it, j) => j===i?{...it, checked: !it.checked}:it));
  return (
    <div className="rs-page" style={{ padding: '16px 16px 24px' }}>
      <h1 className="rs-page-title" style={{ margin: 0 }}>Store</h1>
      <div className="rs-body" style={{ color: 'var(--rs-on-surface-var)', marginTop: 4, marginBottom: 16 }}>5 items · 3 auto-reorder</div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
        {items.map((it, i) => (
          <div key={i} className="rs-card outlined" style={{
            display: 'flex', alignItems: 'center', gap: 12, padding: '12px 14px',
            opacity: it.checked ? 0.55 : 1,
          }}>
            <button onClick={()=>toggle(i)} style={{
              width: 24, height: 24, borderRadius: 9999, border: '2px solid var(--rs-outline)',
              background: it.checked?'var(--rs-primary)':'transparent',
              display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer',
            }}>
              {it.checked && <Icon name="check" sm style={{ fontSize: 14, color: 'var(--rs-on-primary)' }} />}
            </button>
            <div style={{ flex: 1, minWidth: 0 }}>
              <div style={{ fontSize: '0.9375rem', fontWeight: 500, textDecoration: it.checked?'line-through':'none' }}>{it.name}</div>
              <div className="rs-micro" style={{ color: 'var(--rs-on-surface-var)' }}>{it.store}</div>
            </div>
            {it.auto && <span className="rs-badge speak"><Icon name="autorenew" sm style={{ fontSize: 12 }} />AUTO</span>}
          </div>
        ))}
      </div>
      <button className="rs-btn outlined" style={{ marginTop: 18, width: '100%' }}>
        <Icon name="add" sm />Add item
      </button>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// ANALYTICS — system + life metrics
// ─────────────────────────────────────────────────────────────
function AnalyticsScreen() {
  // tiny inline sparkline
  const Spark = ({ values, color }) => {
    const w = 100, h = 36;
    const max = Math.max(...values), min = Math.min(...values);
    const pts = values.map((v, i) => {
      const x = (i / (values.length-1)) * w;
      const y = h - ((v - min) / (max - min || 1)) * h;
      return `${x},${y}`;
    }).join(' ');
    return (
      <svg width={w} height={h} style={{ display: 'block' }}>
        <polyline points={pts} fill="none" stroke={color} strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
      </svg>
    );
  };
  const metrics = [
    { title: 'CONVERSATIONS', value: '24', delta: '+18%', spark: [4,7,5,9,8,11,14], badge: 'CPU' },
    { title: 'LATENCY', value: '380ms', delta: '−42ms', spark: [12,11,9,8,9,7,6], badge: 'GPU' },
    { title: 'MEMORY HITS', value: '89', delta: '+5', spark: [3,4,4,6,7,8,9] },
    { title: 'TOKEN COST', value: '$0.41', delta: 'today', spark: [1,2,1,3,2,4,3], badge: 'COST' },
  ];
  return (
    <div className="rs-page" style={{ padding: '16px 16px 24px' }}>
      <h1 className="rs-page-title" style={{ margin: 0 }}>Analytics</h1>
      <div className="rs-body" style={{ color: 'var(--rs-on-surface-var)', marginTop: 4, marginBottom: 16 }}>Last 7 days</div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12, marginBottom: 16 }}>
        {metrics.map((m, i) => (
          <div key={i} className="rs-card elevated" style={{ padding: 14 }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <div className="rs-micro" style={{ color: 'var(--rs-on-surface-var)' }}>{m.title}</div>
              {m.badge && <span className={`rs-badge ${m.badge.toLowerCase()}`}>{m.badge}</span>}
            </div>
            <div style={{ fontSize: '1.5rem', fontWeight: 400, marginTop: 4 }}>{m.value}</div>
            <div className="rs-micro" style={{ color: 'var(--rs-tertiary)', marginBottom: 6 }}>{m.delta}</div>
            <Spark values={m.spark} color="var(--rs-primary)" />
          </div>
        ))}
      </div>

      <div className="rs-section-title" style={{ marginBottom: 8 }}>System</div>
      <div className="rs-card" style={{ padding: 14 }}>
        {[
          { k: 'Backend', v: 'riversongai.com · OK', dot: 'active' },
          { k: 'Whisper STT', v: 'GTX 1050 Ti · 12% load', dot: 'active' },
          { k: 'Ollama LLM', v: 'CPU · 4.1 GB used', dot: 'standby' },
          { k: 'Piper TTS', v: 'ready · voice "river-amy"', dot: 'active' },
        ].map((r, i) => (
          <div key={i} style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '8px 0', borderTop: i===0?'none':'1px solid var(--rs-outline-variant)' }}>
            <span className={`rs-dot ${r.dot}`} />
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: '0.875rem', fontWeight: 500 }}>{r.k}</div>
              <div className="rs-micro" style={{ color: 'var(--rs-on-surface-var)' }}>{r.v}</div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// LINKS — connected services
// ─────────────────────────────────────────────────────────────
function LinksScreen() {
  const [links, setLinks] = useState([
    { name: 'Home Assistant', desc: '47 entities · LAN', on: true, dot: 'active' },
    { name: 'Google Calendar', desc: 'Sam · personal · work', on: true, dot: 'active' },
    { name: 'Ring Cameras', desc: '4 cameras · cloud', on: true, dot: 'active' },
    { name: 'Spotify', desc: 'Two devices', on: false, dot: 'off' },
    { name: 'Chewy auto-ship', desc: 'Next: May 14', on: true, dot: 'active' },
    { name: 'NWS Weather', desc: 'ZIP 30075', on: true, dot: 'active' },
  ]);
  const flip = (i) => setLinks(links.map((l, j) => j===i ? {...l, on: !l.on, dot: !l.on?'active':'off'} : l));
  return (
    <div className="rs-page" style={{ padding: '16px 16px 24px' }}>
      <h1 className="rs-page-title" style={{ margin: 0 }}>Links</h1>
      <div className="rs-body" style={{ color: 'var(--rs-on-surface-var)', marginTop: 4, marginBottom: 16 }}>Connected services and accounts</div>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
        {links.map((l, i) => (
          <div key={i} className="rs-card" style={{ padding: 14, display: 'flex', alignItems: 'center', gap: 12 }}>
            <div style={{
              width: 40, height: 40, borderRadius: 9999,
              background: 'var(--rs-surface-high)',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              color: l.on ? 'var(--rs-primary)' : 'var(--rs-on-surface-var)',
            }}><Icon name="link" sm /></div>
            <div style={{ flex: 1, minWidth: 0 }}>
              <div style={{ fontSize: '0.9375rem', fontWeight: 500, display: 'flex', alignItems: 'center', gap: 8 }}>
                {l.name} <span className={`rs-dot ${l.dot}`} />
              </div>
              <div className="rs-micro" style={{ color: 'var(--rs-on-surface-var)' }}>{l.desc}</div>
            </div>
            <div className={`rs-switch${l.on?' on':''}`} onClick={()=>flip(i)} />
          </div>
        ))}
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// FEEDS — news, weather, household
// ─────────────────────────────────────────────────────────────
function FeedsScreen() {
  const feeds = [
    { kind: 'WEATHER', title: '68° clear · light rain at 6 PM', body: 'Front porch will be dry till evening. I\'ll bring umbrellas to the front of mind.', when: 'now' },
    { kind: 'BRIEFING', title: 'Morning brief', body: 'Three calendar items, two motion events overnight, one overdue maintenance. No urgent alerts.', when: '7:00 AM' },
    { kind: 'NEWS', title: 'NPR top of hour', body: 'Senate vote scheduled · regional storm system over Tennessee · two science items queued for later.', when: '8:00 AM' },
    { kind: 'HOUSE', title: 'Garage door closed automatically', body: 'You left it open after the trash run. Closed at 8:32.', when: '8:32 AM' },
  ];
  return (
    <div className="rs-page" style={{ padding: '16px 16px 24px' }}>
      <h1 className="rs-page-title" style={{ margin: 0 }}>Feeds</h1>
      <div className="rs-body" style={{ color: 'var(--rs-on-surface-var)', marginTop: 4, marginBottom: 16 }}>Today · pulled together</div>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
        {feeds.map((f, i) => (
          <div key={i} className="rs-card" style={{ padding: 14 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', marginBottom: 4 }}>
              <div className="rs-micro" style={{ color: 'var(--rs-primary)' }}>{f.kind}</div>
              <div className="rs-micro" style={{ color: 'var(--rs-on-surface-var)' }}>{f.when}</div>
            </div>
            <div style={{ fontSize: '1rem', fontWeight: 500, color: 'var(--rs-on-surface)' }}>{f.title}</div>
            <div className="rs-body" style={{ color: 'var(--rs-on-surface-var)', marginTop: 4 }}>{f.body}</div>
          </div>
        ))}
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// READING — saved + queued reading
// ─────────────────────────────────────────────────────────────
function ReadingScreen() {
  const items = [
    { title: 'On the design of household robots', source: 'Long now · 14 min', progress: 0.62, queued: true },
    { title: 'Whisper-cpp on consumer GPUs', source: 'GitHub README · 6 min', progress: 0, queued: true },
    { title: 'Cortana retrospective: voice that knew you', source: 'Halo wiki · 22 min', progress: 0.18, queued: true },
    { title: 'Self-hosting Ollama for a family', source: 'Personal blog · 9 min', progress: 1, queued: false },
  ];
  return (
    <div className="rs-page" style={{ padding: '16px 16px 24px' }}>
      <h1 className="rs-page-title" style={{ margin: 0 }}>Reading</h1>
      <div className="rs-body" style={{ color: 'var(--rs-on-surface-var)', marginTop: 4, marginBottom: 16 }}>Queue · River reads aloud on request</div>

      <div className="rs-section-title" style={{ marginBottom: 8 }}>In progress</div>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 10, marginBottom: 18 }}>
        {items.filter(i=>i.queued && i.progress > 0).map((it, i) => (
          <div key={i} className="rs-card" style={{ padding: 14 }}>
            <div className="rs-micro" style={{ color: 'var(--rs-on-surface-var)' }}>{it.source}</div>
            <div style={{ fontSize: '1rem', fontWeight: 500, marginTop: 4, lineHeight: 1.3 }}>{it.title}</div>
            <div style={{ marginTop: 12, display: 'flex', alignItems: 'center', gap: 10 }}>
              <div style={{ flex: 1, height: 4, borderRadius: 2, background: 'var(--rs-surface-high)' }}>
                <div style={{ width: `${it.progress*100}%`, height: '100%', borderRadius: 2, background: 'var(--rs-primary)' }} />
              </div>
              <div className="rs-micro" style={{ color: 'var(--rs-on-surface-var)' }}>{Math.round(it.progress*100)}%</div>
            </div>
            <div style={{ display: 'flex', gap: 8, marginTop: 12 }}>
              <button className="rs-btn primary" style={{ height: 36, padding: '0 18px' }}>
                <Icon name="play_arrow" sm />Resume
              </button>
              <button className="rs-btn text" style={{ height: 36 }}>Open</button>
            </div>
          </div>
        ))}
      </div>

      <div className="rs-section-title" style={{ marginBottom: 8 }}>Queued</div>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
        {items.filter(i=>i.queued && i.progress === 0).map((it, i) => (
          <div key={i} className="rs-card outlined" style={{ padding: 14 }}>
            <div className="rs-micro" style={{ color: 'var(--rs-on-surface-var)' }}>{it.source}</div>
            <div style={{ fontSize: '0.9375rem', fontWeight: 500, marginTop: 4 }}>{it.title}</div>
          </div>
        ))}
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// LOGIN
// ─────────────────────────────────────────────────────────────
function LoginScreen() {
  const [user, setUser] = useState('');
  const [pw, setPw] = useState('');
  return (
    <div className="rs-page" style={{ padding: '32px 24px', height: '100%', display: 'flex', flexDirection: 'column' }}>
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 32 }}>
          <div style={{
            width: 56, height: 56, borderRadius: 16,
            background: 'var(--rs-primary-c)', color: 'var(--rs-on-primary-c)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            fontFamily: 'Roboto', fontWeight: 600, fontSize: 22, letterSpacing: '0.04em',
          }}>RS</div>
          <div>
            <div style={{ fontSize: '1.5rem', fontWeight: 400 }}>River Song</div>
            <div className="rs-micro" style={{ color: 'var(--rs-on-surface-var)' }}>v0.1 · ALPHA</div>
          </div>
        </div>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
          <input className="rs-tf" placeholder="Username" value={user} onChange={e=>setUser(e.target.value)} />
          <input className="rs-tf" placeholder="Password" type="password" value={pw} onChange={e=>setPw(e.target.value)} />
        </div>
        <button className="rs-btn primary" style={{ marginTop: 28, width: '100%', height: 44 }}>Sign in</button>
        <button className="rs-btn text" style={{ marginTop: 8, width: '100%' }}>Create account</button>

        <div className="rs-card outlined" style={{ marginTop: 32, padding: 12, display: 'flex', alignItems: 'center', gap: 10 }}>
          <span className="rs-dot active" />
          <div style={{ flex: 1 }}>
            <div style={{ fontSize: '0.8125rem', fontWeight: 500 }}>riversongai.com</div>
            <div className="rs-micro" style={{ color: 'var(--rs-on-surface-var)' }}>Connected · Cloudflare proxy</div>
          </div>
        </div>
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// DASHBOARD — user-specific summary
// ─────────────────────────────────────────────────────────────
function DashboardScreen() {
  return (
    <div className="rs-page" style={{ padding: '16px 16px 24px' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 18 }}>
        <div style={{ width: 48, height: 48, borderRadius: 9999, background: 'var(--rs-primary-c)', color: 'var(--rs-on-primary-c)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 600 }}>JC</div>
        <div style={{ flex: 1 }}>
          <h1 className="rs-page-title" style={{ margin: 0, fontSize: '1.5rem' }}>Hello, Jamie</h1>
          <div className="rs-micro" style={{ color: 'var(--rs-on-surface-var)' }}>ADMIN · HOUSEHOLD OWNER</div>
        </div>
        <button className="rs-btn icon-only" style={{ background: 'var(--rs-surface-c)', color: 'var(--rs-on-surface-var)' }}><Icon name="settings" sm /></button>
      </div>

      <div className="rs-card elevated" style={{ padding: 16, marginBottom: 14 }}>
        <div className="rs-section-title" style={{ marginBottom: 10 }}>Today</div>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
          {[
            { t: '10:30 AM', x: 'Vet — Penny (Dr. Allen)' },
            { t: '1:00 PM', x: 'Lunch with Sam' },
            { t: '6:00 PM', x: 'Trash out' },
          ].map((r, i) => (
            <div key={i} style={{ display: 'flex', alignItems: 'baseline', gap: 14 }}>
              <div className="rs-micro" style={{ color: 'var(--rs-primary)', minWidth: 64 }}>{r.t}</div>
              <div className="rs-body" style={{ color: 'var(--rs-on-surface)' }}>{r.x}</div>
            </div>
          ))}
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12, marginBottom: 14 }}>
        <div className="rs-card" style={{ padding: 14 }}>
          <div className="rs-micro" style={{ color: 'var(--rs-on-surface-var)' }}>SMART HOME</div>
          <div style={{ fontSize: '1.5rem', fontWeight: 400, marginTop: 4 }}>14 / 47</div>
          <div className="rs-micro" style={{ color: 'var(--rs-on-surface-var)' }}>active · 2 offline</div>
        </div>
        <div className="rs-card" style={{ padding: 14 }}>
          <div className="rs-micro" style={{ color: 'var(--rs-on-surface-var)' }}>STEPS</div>
          <div style={{ fontSize: '1.5rem', fontWeight: 400, marginTop: 4 }}>3,820</div>
          <div className="rs-micro" style={{ color: 'var(--rs-tertiary)' }}>+12% vs avg</div>
        </div>
      </div>

      <div className="rs-section-title" style={{ marginBottom: 8 }}>What she remembers</div>
      <div className="rs-card outlined" style={{ padding: 14 }}>
        <div className="rs-body" style={{ color: 'var(--rs-on-surface-var)' }}>
          You don't like hedging language. Coffee at 6:45. Penny's vet is Dr. Allen. Sam is allergic to penicillin. Garage door tends to stick in summer.
        </div>
        <button className="rs-btn text" style={{ marginTop: 8, paddingLeft: 0 }}>Open memory →</button>
      </div>
    </div>
  );
}

Object.assign(window, {
  SpeakScreen, ChatScreen, MemoryScreen, InventoryScreen, MaintenanceScreen,
  StoreScreen, AnalyticsScreen, LinksScreen, FeedsScreen, ReadingScreen,
  LoginScreen, DashboardScreen, Icon,
});
