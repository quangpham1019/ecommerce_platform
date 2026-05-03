import React, {useState} from 'react'

async function callApi(path, body){
  const res = await fetch('/api'+path, {method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(body)});
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}

export default function Auth({onAuth, user}){
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [msg, setMsg] = useState('');

  async function register(){
    try{
      const u = await callApi('/register', {email, password});
      onAuth(u);
      setMsg('Registered');
    }catch(e){ setMsg('Register failed: '+e.message) }
  }

  async function login(){
    try{
      const u = await callApi('/login', {email, password});
      onAuth(u);
      setMsg('Logged in');
    }catch(e){ setMsg('Login failed: '+e.message) }
  }

  return (
    <section className="mb-8 bg-white p-4 rounded shadow">
      <h2 className="text-sm font-medium mb-2">Register / Login</h2>
      <div className="flex gap-2">
        <input className="border p-2 flex-1" placeholder="email" value={email} onChange={e=>setEmail(e.target.value)} />
        <input className="border p-2" placeholder="password" type="password" value={password} onChange={e=>setPassword(e.target.value)} />
        <button className="bg-blue-600 text-white px-3 py-2 rounded" onClick={register}>Register</button>
        <button className="bg-green-600 text-white px-3 py-2 rounded" onClick={login}>Login</button>
      </div>
      {msg && <div className="mt-2 text-sm text-gray-600">{msg}</div>}
    </section>
  )
}
