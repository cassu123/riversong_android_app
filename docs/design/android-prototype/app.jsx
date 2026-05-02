/* global React, ReactDOM */
// River Song Android — main app: phone frame + drawer + top bar + bottom nav + tweaks

const { useState, useEffect, useRef } = React;
const Icon = window.Icon;

const NAV_ITEMS = [
  { id: 'speak', label: 'Speak', icon: 'mic' },
  { id: 'chat', label: 'Chat', icon: 'chat_bubble' },
  { id: 'memory', label: 'Memory', icon: 'psychology' },
  { id: 'inventory', label: 'Inventory', icon: 'inventory_2' },
  { id: 'maintenance', label: 'Maintenance', icon: 'build' },
  { id: 'store', label: 'Store', icon: 'shopping_bag' },
  { id: 'analytics', label: 'Analytics', icon: 'bar_chart' },
  { id: 'links', label: 'Links', icon: 'link' },
  { id: 'feeds', label: 'Feeds', icon: 'feed' },
  { id: 'reading', label: 'Reading', icon: 'auto_stories' },
];

const SCREEN_TITLES = {
  speak: 'Speak', chat: 'Chat', memory: 'Memory', inventory: 'Inventory',
  maintenance: 'Maintenance', store: 'Store', analytics: 'Analytics',
  links: 'Links', feeds: 'Feeds', reading: 'Reading',
  login: 'Sign in', dashboard: 'Dashboard',
};

// Tweak defaults — preserved between sessions
const TWEAK_DEFAULTS = /*EDITMODE-BEGIN*/{
  "theme": "default",
  "voiceState": "idle",
  "screen": "speak",
  "showCanvas": false,
  "bottomNavStyle": "compact"
}/*EDITMODE-END*/;

// ─────────────────────────────────────────────────────────────
// Status bar (theme-aware)
// ─────────────────────────────────────────────────────────────
function StatusBar() {
  return (
    <div style={{
      height: 32, display: 'flex', alignItems: 'center', justifyContent: 'space-between',
      padding: '0 18px', position: 'relative', flexShrink: 0,
      color: 'var(--rs-on-surface)',
    }}>
      <div style={{ fontSize: 13, fontWeight: 500, letterSpacing: 0.25 }}>9:30</div>
      <div style={{
        position: 'absolute', left: '50%', top: 6, transform: 'translateX(-50%)',
        width: 18, height: 18, borderRadius: 100, background: '#000',
      }} />
      <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
        <Icon name="signal_cellular_alt" sm style={{ fontSize: 14 }} />
        <Icon name="wifi" sm style={{ fontSize: 14 }} />
        <Icon name="battery_full" sm style={{ fontSize: 14 }} />
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// Top app bar
// ─────────────────────────────────────────────────────────────
function TopBar({ title, onMenu, screen }) {
  return (
    <div style={{
      height: 64, display: 'flex', alignItems: 'center', gap: 4,
      padding: '0 4px', flexShrink: 0,
      background: 'var(--rs-surface)',
    }}>
      <button onClick={onMenu} className="rs-btn icon-only" style={{ background: 'transparent', color: 'var(--rs-on-surface)' }}>
        <Icon name="menu" />
      </button>
      <div style={{
        flex: 1, fontSize: 22, fontWeight: 400, color: 'var(--rs-on-surface)',
      }}>{title}</div>
      {screen === 'speak' && (
        <button className="rs-btn icon-only" style={{ background: 'transparent', color: 'var(--rs-on-surface-var)' }}>
          <Icon name="more_vert" />
        </button>
      )}
      {screen !== 'speak' && (
        <button className="rs-btn icon-only" style={{ background: 'transparent', color: 'var(--rs-on-surface-var)' }}>
          <Icon name="search" />
        </button>
      )}
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// Bottom nav (4 quick tabs)
// ─────────────────────────────────────────────────────────────
function BottomNav({ screen, setScreen }) {
  const tabs = [
    { id: 'speak', label: 'Speak', icon: 'mic' },
    { id: 'chat', label: 'Chat', icon: 'chat_bubble' },
    { id: 'feeds', label: 'Feeds', icon: 'feed' },
    { id: 'dashboard', label: 'You', icon: 'person' },
  ];
  return (
    <div className="rs-bottomnav" style={{ flexShrink: 0 }}>
      {tabs.map(t => (
        <div key={t.id} className={`rs-bottomnav-item${screen===t.id?' active':''}`} onClick={()=>setScreen(t.id)}>
          <div className="pill"><Icon name={t.icon} sm style={{ fontSize: 22 }} /></div>
          <div>{t.label}</div>
        </div>
      ))}
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// Nav drawer overlay
// ─────────────────────────────────────────────────────────────
function NavDrawer({ open, onClose, screen, setScreen }) {
  return (
    <>
      {open && (
        <div onClick={onClose} style={{
          position: 'absolute', inset: 0, background: 'var(--rs-scrim)', zIndex: 50,
          animation: 'rs-page-in 200ms',
        }} />
      )}
      <div style={{
        position: 'absolute', top: 0, bottom: 0, left: 0, width: 280,
        background: 'var(--rs-surface-low)',
        zIndex: 51,
        transform: open ? 'translateX(0)' : 'translateX(-100%)',
        transition: 'transform 250ms cubic-bezier(0.4,0,0.2,1)',
        display: 'flex', flexDirection: 'column',
        borderRight: '1px solid var(--rs-outline-variant)',
        overflow: 'hidden',
      }}>
        {/* header */}
        <div style={{ padding: '24px 20px 16px', display: 'flex', alignItems: 'center', gap: 12 }}>
          <div style={{
            width: 44, height: 44, borderRadius: 12,
            background: 'var(--rs-primary-c)', color: 'var(--rs-on-primary-c)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            fontWeight: 600, letterSpacing: '0.06em',
          }}>RS</div>
          <div>
            <div style={{ fontSize: '1.0625rem', fontWeight: 500, color: 'var(--rs-on-surface)' }}>River Song</div>
            <div className="rs-micro" style={{ color: 'var(--rs-on-surface-var)' }}>v0.1 · ALPHA</div>
          </div>
        </div>
        <div style={{ height: 1, background: 'var(--rs-outline-variant)', margin: '0 12px 8px' }} />
        {/* nav list */}
        <div style={{ flex: 1, overflow: 'auto', padding: '4px 12px' }}>
          {NAV_ITEMS.map(n => (
            <div key={n.id} className={`rs-nav-item${screen===n.id?' active':''}`} onClick={()=>{ setScreen(n.id); onClose(); }}>
              <Icon name={n.icon} sm style={{ fontSize: 22 }} />
              <span>{n.label}</span>
            </div>
          ))}
        </div>
        <div style={{ height: 1, background: 'var(--rs-outline-variant)', margin: '0 12px' }} />
        <div style={{ padding: '8px 12px 18px' }}>
          <div className="rs-nav-item" onClick={()=>{ setScreen('dashboard'); onClose(); }}>
            <Icon name="person" sm style={{ fontSize: 22 }} />
            <span>Dashboard</span>
          </div>
          <div className="rs-nav-item">
            <Icon name="settings" sm style={{ fontSize: 22 }} />
            <span>Settings</span>
          </div>
        </div>
      </div>
    </>
  );
}

// ─────────────────────────────────────────────────────────────
// Phone shell — wraps a single device with all chrome
// ─────────────────────────────────────────────────────────────
function PhoneShell({ theme, screen, setScreen, voiceState, setVoiceState, scale = 1 }) {
  const [drawerOpen, setDrawerOpen] = useState(false);
  const W = 384, H = 820;

  // mock transcript text used by Speak
  const transcript = 'turn on the kitchen lights and start the coffee';

  const showChrome = screen !== 'login';
  const ScreenComp = {
    speak: () => <window.SpeakScreen voiceState={voiceState} setVoiceState={setVoiceState} transcript={transcript} />,
    chat: () => <window.ChatScreen />,
    memory: () => <window.MemoryScreen />,
    inventory: () => <window.InventoryScreen />,
    maintenance: () => <window.MaintenanceScreen />,
    store: () => <window.StoreScreen />,
    analytics: () => <window.AnalyticsScreen />,
    links: () => <window.LinksScreen />,
    feeds: () => <window.FeedsScreen />,
    reading: () => <window.ReadingScreen />,
    login: () => <window.LoginScreen />,
    dashboard: () => <window.DashboardScreen />,
  }[screen] || (() => <window.SpeakScreen voiceState={voiceState} setVoiceState={setVoiceState} transcript={transcript} />);

  return (
    <div data-theme={theme} className="rs scanlines" style={{
      width: W, height: H,
      transform: `scale(${scale})`, transformOrigin: 'top center',
      borderRadius: 38, overflow: 'hidden',
      background: 'var(--rs-bg)',
      border: '7px solid #1a1a1a',
      boxShadow: '0 30px 80px rgba(0,0,0,0.45), 0 0 0 1px rgba(255,255,255,0.03) inset',
      position: 'relative',
      display: 'flex', flexDirection: 'column',
    }}>
      <StatusBar />
      {showChrome && <TopBar title={SCREEN_TITLES[screen]} onMenu={()=>setDrawerOpen(true)} screen={screen} />}
      <div style={{ flex: 1, overflow: 'auto', position: 'relative' }} key={screen}>
        <ScreenComp />
      </div>
      {showChrome && <BottomNav screen={screen} setScreen={setScreen} />}
      {/* gesture pill */}
      <div style={{ height: 24, display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0, background: 'var(--rs-surface-c)' }}>
        <div style={{ width: 108, height: 4, borderRadius: 2, background: 'var(--rs-on-surface)', opacity: 0.4 }} />
      </div>
      <NavDrawer open={drawerOpen} onClose={()=>setDrawerOpen(false)} screen={screen} setScreen={setScreen} />
    </div>
  );
}

// ─────────────────────────────────────────────────────────────
// Tweaks panel
// ─────────────────────────────────────────────────────────────
const THEMES = [
  { id: 'default', label: 'River Song Blue', swatch: '#96CBFF', dark: true },
  { id: 'halo', label: 'Halo', swatch: '#00e5ff', dark: true },
  { id: 'crimson', label: 'Crimson Dark', swatch: '#ff8b73', dark: true },
  { id: 'combat', label: 'Combat', swatch: '#3dcc79', dark: true },
  { id: 'violet', label: 'Midnight Violet', swatch: '#d4a8d6', dark: true },
  { id: 'peach', label: 'Peach Dream', swatch: '#D66C59', dark: false },
  { id: 'arctic', label: 'Arctic', swatch: '#4A7AA8', dark: false },
  { id: 'cyberpunk', label: 'Cyberpunk', swatch: '#e8ff00', dark: true },
  { id: 'dune', label: 'Dune', swatch: '#deb651', dark: true },
];

const VOICE_STATES = ['idle','connecting','listening','transcribing','thinking','speaking','error'];

function AppTweaks({ tweaks, setTweak }) {
  const { TweaksPanel, TweakSection, TweakSelect, TweakRadio, TweakToggle } = window;
  if (!TweaksPanel) return null;
  return (
    <TweaksPanel title="Tweaks">
      <TweakSection title="View">
        <TweakRadio
          label="Layout"
          value={tweaks.showCanvas ? 'canvas' : 'phone'}
          options={[{value:'phone', label:'Phone'},{value:'canvas', label:'Canvas'}]}
          onChange={v => setTweak('showCanvas', v === 'canvas')}
        />
        <TweakSelect
          label="Screen"
          value={tweaks.screen}
          options={[
            ...NAV_ITEMS.map(n => ({ value: n.id, label: n.label })),
            { value: 'dashboard', label: 'Dashboard' },
            { value: 'login', label: 'Login' },
          ]}
          onChange={v => setTweak('screen', v)}
        />
      </TweakSection>

      <TweakSection title="Theme">
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 6 }}>
          {THEMES.map(t => (
            <button key={t.id} onClick={()=>setTweak('theme', t.id)} style={{
              display: 'flex', alignItems: 'center', gap: 8,
              padding: '8px 10px', borderRadius: 8,
              border: tweaks.theme === t.id ? '1.5px solid #96CBFF' : '1px solid rgba(255,255,255,0.12)',
              background: tweaks.theme === t.id ? 'rgba(150,203,255,0.10)' : 'transparent',
              color: '#DEE4E9', cursor: 'pointer', textAlign: 'left',
              fontFamily: 'Roboto, system-ui', fontSize: 12, fontWeight: 500,
            }}>
              <span style={{
                width: 16, height: 16, borderRadius: 4, background: t.swatch,
                boxShadow: '0 0 0 1px rgba(255,255,255,0.15) inset',
                flexShrink: 0,
              }} />
              <span style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{t.label}</span>
            </button>
          ))}
        </div>
      </TweakSection>

      <TweakSection title="Voice state (Speak screen)">
        <TweakSelect
          label="State"
          value={tweaks.voiceState}
          options={VOICE_STATES.map(s => ({ value: s, label: s }))}
          onChange={v => setTweak('voiceState', v)}
        />
      </TweakSection>
    </TweaksPanel>
  );
}

// ─────────────────────────────────────────────────────────────
// Design canvas — all screens at once
// ─────────────────────────────────────────────────────────────
function CanvasView({ theme, voiceState }) {
  const { DCRoot, DCSection, DCArtboard } = window;
  if (!DCRoot) return <div style={{ padding: 40, color: '#fff' }}>Loading canvas…</div>;

  const screens = [
    { id: 'speak', label: 'Speak — IDLE' },
    { id: 'speak-listening', label: 'Speak — LISTENING', screen: 'speak', state: 'listening' },
    { id: 'speak-thinking', label: 'Speak — THINKING', screen: 'speak', state: 'thinking' },
    { id: 'speak-speaking', label: 'Speak — SPEAKING', screen: 'speak', state: 'speaking' },
    { id: 'chat', label: 'Chat' },
    { id: 'memory', label: 'Memory' },
    { id: 'inventory', label: 'Inventory' },
    { id: 'maintenance', label: 'Maintenance' },
    { id: 'store', label: 'Store' },
    { id: 'analytics', label: 'Analytics' },
    { id: 'links', label: 'Links' },
    { id: 'feeds', label: 'Feeds' },
    { id: 'reading', label: 'Reading' },
    { id: 'dashboard', label: 'Dashboard' },
    { id: 'login', label: 'Login' },
  ];

  // Sub-app inside an artboard
  const ArtboardPhone = ({ id, screen, state }) => {
    const [s, setS] = useState(screen || id);
    const [vs, setVs] = useState(state || 'idle');
    return <PhoneShell theme={theme} screen={s} setScreen={setS} voiceState={vs} setVoiceState={setVs} />;
  };

  return (
    <DCRoot title="River Song Android — All Screens" subtitle="Drag artboards · click to focus">
      <DCSection id="primary" title="Primary surfaces">
        {['speak','chat','dashboard','feeds'].map(id => {
          const s = screens.find(x => x.id === id);
          return (
            <DCArtboard key={id} id={id} label={s.label} width={398} height={834}>
              <ArtboardPhone id={id} />
            </DCArtboard>
          );
        })}
      </DCSection>
      <DCSection id="voice-states" title="Speak — voice states">
        {['speak-listening','speak-thinking','speak-speaking'].map(id => {
          const s = screens.find(x => x.id === id);
          return (
            <DCArtboard key={id} id={id} label={s.label} width={398} height={834}>
              <ArtboardPhone id={id} screen={s.screen} state={s.state} />
            </DCArtboard>
          );
        })}
      </DCSection>
      <DCSection id="utility" title="Utility surfaces">
        {['memory','inventory','maintenance','store'].map(id => {
          const s = screens.find(x => x.id === id);
          return (
            <DCArtboard key={id} id={id} label={s.label} width={398} height={834}>
              <ArtboardPhone id={id} />
            </DCArtboard>
          );
        })}
      </DCSection>
      <DCSection id="meta" title="Analytics, links & reading">
        {['analytics','links','reading','login'].map(id => {
          const s = screens.find(x => x.id === id);
          return (
            <DCArtboard key={id} id={id} label={s.label} width={398} height={834}>
              <ArtboardPhone id={id} />
            </DCArtboard>
          );
        })}
      </DCSection>
    </DCRoot>
  );
}

// ─────────────────────────────────────────────────────────────
// Root app
// ─────────────────────────────────────────────────────────────
function App() {
  const { useTweaks } = window;
  const [tweaks, setTweak] = useTweaks ? useTweaks(TWEAK_DEFAULTS) : [TWEAK_DEFAULTS, () => {}];

  // Voice state lives in tweaks for control, but local state on Speak handles user taps too
  const [localVoice, setLocalVoice] = useState(tweaks.voiceState);
  useEffect(() => { setLocalVoice(tweaks.voiceState); }, [tweaks.voiceState]);
  const setVoice = (s) => { setLocalVoice(s); setTweak('voiceState', s); };

  const [localScreen, setLocalScreen] = useState(tweaks.screen);
  useEffect(() => { setLocalScreen(tweaks.screen); }, [tweaks.screen]);
  const setScreen = (s) => { setLocalScreen(s); setTweak('screen', s); };

  return (
    <>
      <div style={{
        minHeight: '100vh',
        display: 'flex', alignItems: 'flex-start', justifyContent: 'center',
        padding: tweaks.showCanvas ? 0 : '32px 16px',
        background: '#0a0d10',
      }}>
        {tweaks.showCanvas ? (
          <CanvasView theme={tweaks.theme} voiceState={localVoice} />
        ) : (
          <div style={{ marginTop: 8 }}>
            <PhoneShell
              theme={tweaks.theme}
              screen={localScreen}
              setScreen={setScreen}
              voiceState={localVoice}
              setVoiceState={setVoice}
            />
            <div style={{
              textAlign: 'center', marginTop: 18,
              fontFamily: 'Roboto, system-ui', color: '#5a6470', fontSize: 11, letterSpacing: '0.12em', textTransform: 'uppercase',
            }}>
              River Song Android · {THEMES.find(t => t.id===tweaks.theme)?.label}
            </div>
          </div>
        )}
      </div>
      <AppTweaks tweaks={tweaks} setTweak={setTweak} />
    </>
  );
}

ReactDOM.createRoot(document.getElementById('root')).render(<App />);
