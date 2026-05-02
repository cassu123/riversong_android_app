/* global React */
// HoloBust — animated holographic portrait for the Speak screen.
// Renders to a canvas, state-driven color, gentle aura + breathing scanlines.

const { useEffect, useRef } = React;

const STATE_COLORS = {
  idle:        { core: '#96CBFF', aura: '#96CBFF' },
  connecting:  { core: '#5B7FA3', aura: '#96CBFF' },
  listening:   { core: '#D9BBFF', aura: '#D9BBFF' },
  transcribing:{ core: '#82C8E5', aura: '#96CBFF' },
  thinking:    { core: '#C4AAFF', aura: '#9955FF' },
  speaking:    { core: '#82E1B7', aura: '#82E1B7' },
  error:       { core: '#FFB4AB', aura: '#FF6B5E' },
};

function HoloBust({ state = 'idle', size = 220, audioLevel = 0 }) {
  const canvasRef = useRef(null);
  const stateRef = useRef(state);
  const audioRef = useRef(audioLevel);
  useEffect(() => { stateRef.current = state; }, [state]);
  useEffect(() => { audioRef.current = audioLevel; }, [audioLevel]);

  useEffect(() => {
    const cvs = canvasRef.current;
    if (!cvs) return;
    const dpr = window.devicePixelRatio || 1;
    cvs.width = size * dpr; cvs.height = size * dpr;
    const ctx = cvs.getContext('2d');
    ctx.scale(dpr, dpr);
    let raf, t0 = performance.now();

    const draw = (now) => {
      const t = (now - t0) / 1000;
      const s = stateRef.current;
      const colors = STATE_COLORS[s] || STATE_COLORS.idle;
      const cx = size / 2, cy = size / 2;
      ctx.clearRect(0, 0, size, size);

      // outer aura
      const auraR = size * 0.48 + Math.sin(t * 1.6) * 4 + (s === 'thinking' ? Math.sin(t*3.5)*6 : 0);
      const grad = ctx.createRadialGradient(cx, cy, size*0.18, cx, cy, auraR);
      grad.addColorStop(0, colors.aura + '55');
      grad.addColorStop(0.6, colors.aura + '18');
      grad.addColorStop(1, colors.aura + '00');
      ctx.fillStyle = grad;
      ctx.beginPath(); ctx.arc(cx, cy, auraR, 0, Math.PI*2); ctx.fill();

      // orbit ring
      ctx.strokeStyle = colors.core + '55';
      ctx.lineWidth = 1;
      ctx.beginPath();
      const orbitR = size * 0.42;
      ctx.ellipse(cx, cy, orbitR, orbitR * 0.32, t * (s==='thinking'?1.2:0.4), 0, Math.PI*2);
      ctx.stroke();
      ctx.beginPath();
      ctx.ellipse(cx, cy, orbitR*0.86, orbitR * 0.22, -t * 0.6, 0, Math.PI*2);
      ctx.strokeStyle = colors.core + '33';
      ctx.stroke();

      // bust silhouette path (stylized head + shoulders)
      ctx.save();
      ctx.translate(cx, cy);

      // head outline
      const headR = size * 0.18;
      ctx.beginPath();
      ctx.moveTo(-headR*1.3, headR*1.7);
      ctx.bezierCurveTo(-headR*1.3, headR*0.7, -headR, headR*0.5, -headR*0.7, headR*0.4);
      ctx.lineTo(-headR*0.7, 0);
      ctx.bezierCurveTo(-headR*1.05, -headR*0.1, -headR*1.05, -headR*1.05, -headR*0.1, -headR*1.4);
      ctx.bezierCurveTo(headR*0.95, -headR*1.4, headR*1.05, -headR*0.4, headR*0.7, 0);
      ctx.lineTo(headR*0.7, headR*0.4);
      ctx.bezierCurveTo(headR, headR*0.5, headR*1.3, headR*0.7, headR*1.3, headR*1.7);
      ctx.closePath();

      // soft fill
      const headGrad = ctx.createLinearGradient(0, -headR, 0, headR*2);
      headGrad.addColorStop(0, colors.core + '22');
      headGrad.addColorStop(1, colors.core + '08');
      ctx.fillStyle = headGrad;
      ctx.fill();
      // edge stroke
      ctx.lineWidth = 1.2;
      ctx.strokeStyle = colors.core;
      ctx.stroke();

      // hair/crown lines
      ctx.lineWidth = 0.8;
      ctx.strokeStyle = colors.core + 'AA';
      for (let i=-3; i<=3; i++) {
        ctx.beginPath();
        ctx.moveTo(i*headR*0.18, -headR*1.1);
        ctx.lineTo(i*headR*0.22, -headR*1.4 + Math.sin(t*0.8 + i)*1.5);
        ctx.stroke();
      }

      // eye line + eyes
      const blink = (Math.sin(t*0.7) > 0.97) ? 0.15 : 1;
      ctx.fillStyle = colors.core;
      ctx.globalAlpha = 0.9;
      const eyeY = -headR*0.3;
      ctx.beginPath(); ctx.ellipse(-headR*0.32, eyeY, headR*0.08, headR*0.05*blink, 0, 0, Math.PI*2); ctx.fill();
      ctx.beginPath(); ctx.ellipse(headR*0.32, eyeY, headR*0.08, headR*0.05*blink, 0, 0, Math.PI*2); ctx.fill();
      ctx.globalAlpha = 1;

      // nose
      ctx.strokeStyle = colors.core + '88';
      ctx.lineWidth = 0.8;
      ctx.beginPath();
      ctx.moveTo(0, eyeY + headR*0.05);
      ctx.lineTo(-headR*0.05, eyeY + headR*0.35);
      ctx.lineTo(headR*0.04, eyeY + headR*0.35);
      ctx.stroke();

      // mouth (lip-sync amplitude when speaking)
      const speakAmp = (s === 'speaking') ? (0.4 + audioRef.current*0.6) : 0.1;
      const mouthY = eyeY + headR*0.7;
      ctx.lineWidth = 1.4;
      ctx.beginPath();
      ctx.moveTo(-headR*0.22, mouthY);
      ctx.quadraticCurveTo(0, mouthY + headR*0.2*speakAmp, headR*0.22, mouthY);
      ctx.strokeStyle = colors.core;
      ctx.stroke();

      // shoulder collar lines
      ctx.lineWidth = 1;
      ctx.strokeStyle = colors.core + 'CC';
      ctx.beginPath();
      ctx.moveTo(-headR*0.7, headR*0.6);
      ctx.lineTo(-headR*0.4, headR*1.0);
      ctx.lineTo(0, headR*1.05);
      ctx.lineTo(headR*0.4, headR*1.0);
      ctx.lineTo(headR*0.7, headR*0.6);
      ctx.stroke();

      // hud ticks under chin
      ctx.strokeStyle = colors.core + '55';
      for (let i=-4; i<=4; i++) {
        ctx.beginPath();
        ctx.moveTo(i*headR*0.18, headR*1.25);
        ctx.lineTo(i*headR*0.18, headR*1.25 + (i%2===0?6:3));
        ctx.stroke();
      }

      // scanlines clipped to head
      ctx.save();
      ctx.beginPath();
      ctx.ellipse(0, -headR*0.3, headR*1.05, headR*1.3, 0, 0, Math.PI*2);
      ctx.clip();
      ctx.strokeStyle = colors.core + '20';
      const offset = (t * 14) % 6;
      for (let y = -headR*1.5 - offset; y < headR*1.5; y += 3) {
        ctx.beginPath();
        ctx.moveTo(-headR*1.3, y); ctx.lineTo(headR*1.3, y);
        ctx.stroke();
      }
      ctx.restore();
      ctx.restore();

      raf = requestAnimationFrame(draw);
    };
    raf = requestAnimationFrame(draw);
    return () => cancelAnimationFrame(raf);
  }, [size]);

  return (
    <canvas ref={canvasRef} style={{ width: size, height: size, display: 'block' }} />
  );
}

// Audio visualizer — 24 radial bars, reactive on speaking
function AudioRing({ state = 'idle', size = 280 }) {
  const canvasRef = useRef(null);
  const stateRef = useRef(state);
  useEffect(() => { stateRef.current = state; }, [state]);

  useEffect(() => {
    const cvs = canvasRef.current; if (!cvs) return;
    const dpr = window.devicePixelRatio || 1;
    cvs.width = size * dpr; cvs.height = size * dpr;
    const ctx = cvs.getContext('2d');
    ctx.scale(dpr, dpr);
    let raf, t0 = performance.now();
    const N = 32;
    const draw = (now) => {
      const t = (now - t0) / 1000;
      const s = stateRef.current;
      const colors = STATE_COLORS[s] || STATE_COLORS.idle;
      ctx.clearRect(0, 0, size, size);
      const cx = size/2, cy = size/2;
      const baseR = size * 0.40;
      for (let i=0; i<N; i++) {
        const ang = (i / N) * Math.PI * 2 - Math.PI/2;
        let amp = 6;
        if (s === 'listening') amp = 6 + Math.abs(Math.sin(t*4 + i*0.6)) * 12;
        else if (s === 'thinking') amp = 4 + Math.abs(Math.sin(t*2 + i*0.3)) * 6;
        else if (s === 'speaking') amp = 8 + Math.abs(Math.sin(t*8 + i*0.9)) * 24;
        else amp = 4 + Math.abs(Math.sin(t*1.2 + i*0.4)) * 3;
        const x1 = cx + Math.cos(ang) * baseR;
        const y1 = cy + Math.sin(ang) * baseR;
        const x2 = cx + Math.cos(ang) * (baseR + amp);
        const y2 = cy + Math.sin(ang) * (baseR + amp);
        ctx.strokeStyle = colors.core + (s==='idle'?'55':'AA');
        ctx.lineWidth = 2; ctx.lineCap = 'round';
        ctx.beginPath(); ctx.moveTo(x1, y1); ctx.lineTo(x2, y2); ctx.stroke();
      }
      raf = requestAnimationFrame(draw);
    };
    raf = requestAnimationFrame(draw);
    return () => cancelAnimationFrame(raf);
  }, [size]);
  return <canvas ref={canvasRef} style={{ width: size, height: size, position: 'absolute', inset: 0, margin: 'auto' }} />;
}

window.HoloBust = HoloBust;
window.AudioRing = AudioRing;
