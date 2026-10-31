/** Stylised homage to the Mohamed Ali barrages in El Qanater, rather than a measured drawing. */
export default function BarragesArtwork({label}:{label:string}) {
  return <svg viewBox="0 0 520 460" fill="none" role="img" aria-label={label}>
    <defs>
      <linearGradient id="kapinda-barrage-sky" x1="260" y1="35" x2="260" y2="420" gradientUnits="userSpaceOnUse"><stop stopColor="#245d64"/><stop offset="1" stopColor="#10383e"/></linearGradient>
      <linearGradient id="kapinda-nile" x1="260" y1="315" x2="260" y2="420" gradientUnits="userSpaceOnUse"><stop stopColor="#366b6c"/><stop offset="1" stopColor="#103f46"/></linearGradient>
      <pattern id="kapinda-barrage-tiles" width="36" height="36" patternUnits="userSpaceOnUse"><path d="m18 0 18 18-18 18L0 18Zm0 9 9 9-9 9-9-9Z" stroke="#C4AA73" strokeWidth=".5" opacity=".22"/></pattern>
      <clipPath id="kapinda-barrage-window"><path d="M37 425V188c0-80 145-85 223-160 78 75 223 80 223 160v237Z"/></clipPath>
    </defs>
    <path d="M24 438V187C24 97 174 92 260 10c86 82 236 87 236 177v251Z" stroke="#CBAE73" strokeWidth="1.2"/>
    <path d="M37 425V188c0-80 145-85 223-160 78 75 223 80 223 160v237Z" fill="url(#kapinda-barrage-sky)" stroke="#CBAE73" strokeWidth=".7"/>
    <g clipPath="url(#kapinda-barrage-window)">
      <path d="M37 25h446v400H37Z" fill="url(#kapinda-barrage-tiles)"/>
      <circle cx="260" cy="137" r="43" stroke="#CBAE73" strokeWidth=".5" opacity=".6"/>
      <path d="M275 107c-35 1-43 44-10 56-40 5-51-47-18-58-5 13 10 12 28 2Z" fill="#D4BA80"/>
      <path d="m307 123 2 6 6 2-6 2-2 6-2-6-6-2 6-2Z" fill="#D4BA80"/>
      <path d="M37 339c78-16 124 17 217 0s148-10 229 3v96H37Z" fill="url(#kapinda-nile)"/>
      <path d="M59 351c83 6 103 18 191 7s135-9 208 4M74 371c60-2 96 14 180 3s132-7 190 4M105 394c57 0 94 8 151 1s104-7 155-1" stroke="#CBB47D" strokeWidth=".8" opacity=".55"/>
      <path d="M48 314h424v29H48Z" fill="#8B7752" stroke="#D1B37A" strokeWidth="1.1"/>
      <path d="M57 255h406v77H57Z" fill="#9F8052" stroke="#D4B67B" strokeWidth="1.2"/>
      <path d="M62 265h396M62 275h396M62 284h396" stroke="#D9BC83" strokeWidth=".6" opacity=".6"/>
      {[80,120,160,200,240,280,320,360,400].map(x=><g key={x}><path d={`M${x} 334v-28a16 16 0 0 1 32 0v28Z`} fill="#174A50" stroke="#E0C28A" strokeWidth="1.25"/><path d={`M${x-3} 334v-28a19 19 0 0 1 38 0v28`} stroke="#D3B57A" strokeWidth=".6"/><path d={`M${x+9} 346h15m-12 8h20`} stroke="#CFB378" strokeWidth=".7" opacity=".6"/></g>)}
      <path d="M53 253h414v6H53Z" fill="#D0AE70"/><path d="M62 246h396" stroke="#D4B579" strokeWidth="1.1"/>
      {Array.from({length:29},(_,index)=>index*13+71).map(x=><path key={x} d={`M${x} 247v7`} stroke="#D8BC80" strokeWidth="1"/>)}
      {[48,429].map(x=><g key={x} transform={`translate(${x} 193)`}><path d="M0 18H7V8H15V18H24V8H32V18H40V8H48V18H55V145H0Z" fill="#9E8055" stroke="#D9B97E" strokeWidth="1.3"/><path d="M-3 27h61M-3 32h61M0 119h55M0 125h55M8 36v77m39-77v77" stroke="#D4B57E" strokeWidth=".7"/><path d="M20 145v-30c0-14 15-14 15 0v30M22 52v16h10V52a5 5 0 0 0-10 0Zm0 30v16h10V82a5 5 0 0 0-10 0Z" fill="#19494E" stroke="#D5B67D" strokeWidth="1"/></g>)}
      <path d="M36 336h448M38 340h444" stroke="#D6B87C" strokeWidth="1.1"/>
      <path d="M37 387c35-21 71-8 87 38H37m446-33c-32-17-54-4-73 33h73" fill="#1D5152"/>
      <path d="M65 387c-1-24-4-43-3-64m0 2c-10-18-22-18-29-10 15-3 22 3 29 10Zm0 0c10-17 21-18 30-9-15-3-23 3-30 9Zm0-1c-3-16 3-25 10-28-6 12-7 20-10 28Z" stroke="#C5AF78" strokeWidth=".8" opacity=".8"/>
    </g>
    <path d="M14 440h492M171 451h178" stroke="#CBAE73" strokeWidth=".8"/><path d="m253 448 7-5 7 5-7 5Z" fill="#CBAE73"/>
  </svg>;
}
