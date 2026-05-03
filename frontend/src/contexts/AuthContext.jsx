import React, {createContext, useContext, useState, useEffect} from 'react'
import { useNavigate } from 'react-router-dom'

const AuthContext = createContext(null)

export function AuthProvider({children}){
  const [user, setUser] = useState(null)
  const [isAuthenticated, setIsAuthenticated] = useState(false)

  useEffect(()=>{
    // attempt to load current user from API
    (async ()=>{
      try{
        const res = await fetch('/api/me')
        if (res.ok){
          const me = await res.json()
          setUser(me)
          setIsAuthenticated(true)
        }
      }catch(e){}
    })()
  }, [])

  async function login(email, password){
    const res = await fetch('/api/login', {method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({email,password})})
    if (!res.ok) throw new Error('Login failed')
    const me = await res.json()
    setUser(me)
    setIsAuthenticated(true)
  }

  async function register(email, password){
    const res = await fetch('/api/register', {method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({email,password})})
    if (!res.ok) throw new Error('Register failed')
    const me = await res.json()
    setUser(me)
    setIsAuthenticated(true)
  }

  async function logout(){
    await fetch('/api/logout', {method:'POST'})
    setUser(null)
    setIsAuthenticated(false)
  }

  return (
    <AuthContext.Provider value={{user, isAuthenticated, login, register, logout}}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth(){ return useContext(AuthContext) }
