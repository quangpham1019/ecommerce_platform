import React, {useState} from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'

export default function Register(){
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [err, setErr] = useState(null)
  const { register } = useAuth()
  const nav = useNavigate()

  async function handle(e){
    e.preventDefault()
    try{
      await register(email, password)
      nav('/')
    }catch(e){ setErr(e.message) }
  }

  return (
    <div className="max-w-md mx-auto">
      <h2 className="text-xl font-semibold mb-4">Register</h2>
      <form onSubmit={handle} className="space-y-4">
        <input value={email} onChange={e=>setEmail(e.target.value)} placeholder="Email" className="w-full p-2 border rounded" />
        <input value={password} onChange={e=>setPassword(e.target.value)} type="password" placeholder="Password" className="w-full p-2 border rounded" />
        {err && <div className="text-sm text-red-600">{err}</div>}
        <div className="flex justify-between items-center">
          <button className="bg-green-600 text-white px-4 py-2 rounded">Register</button>
        </div>
      </form>
    </div>
  )
}
