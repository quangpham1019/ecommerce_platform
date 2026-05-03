import React, {useState, useEffect} from 'react'
import Auth from './pages/Auth'
import Home from './pages/Home'

export default function App(){
  const [user, setUser] = useState(()=>{
    const raw = localStorage.getItem('user');
    return raw ? JSON.parse(raw) : null;
  })

  useEffect(()=>{
    if(user) localStorage.setItem('user', JSON.stringify(user));
    else localStorage.removeItem('user');
  }, [user])

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow">
        <div className="max-w-4xl mx-auto py-4 px-6 flex justify-between items-center">
          <h1 className="text-lg font-semibold">Marketplace</h1>
          <div>
            {user ? <span className="text-sm">{user.email} (id={user.id})</span> : <span className="text-sm text-gray-500">Not signed in</span>}
          </div>
        </div>
      </header>
      <main className="max-w-4xl mx-auto p-6">
        <Auth onAuth={setUser} user={user} />
        <Home user={user} />
      </main>
    </div>
  )
}
