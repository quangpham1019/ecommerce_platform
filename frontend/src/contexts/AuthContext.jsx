import React, {createContext, useContext, useState, useEffect} from 'react'

const AuthContext = createContext(null)

export function AuthProvider({children}){
  const [user, setUser] = useState(null)
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [sellerProfile, setSellerProfile] = useState(null)

  async function loadSellerProfile(){
    try{
      const res = await fetch('/api/seller-profiles/me', {credentials: 'include'})
      if (res.ok){
        const profile = await res.json()
        setSellerProfile(profile)
      } else {
        setSellerProfile(null)
      }
    }catch(e){
      setSellerProfile(null)
    }
  }

  useEffect(()=>{
    // attempt to load current user from API
    (async ()=>{
      try{
        const res = await fetch('/api/me', {credentials: 'include'})
        if (res.ok){
          const me = await res.json()
          setUser(me)
          setIsAuthenticated(true)
          await loadSellerProfile()
        }
      }catch(e){}
    })()
  }, [])

  async function login(email, password){
    const res = await fetch('/api/login', {
      method:'POST',
      credentials: 'include',
      headers:{'Content-Type':'application/json'},
      body: JSON.stringify({email,password})
    })
    if (!res.ok) throw new Error('Login failed')
    const me = await res.json()
    setUser(me)
    setIsAuthenticated(true)
    await loadSellerProfile()
  }

  async function register(email, password){
    const res = await fetch('/api/register', {
      method:'POST',
      credentials: 'include',
      headers:{'Content-Type':'application/json'},
      body: JSON.stringify({email,password})
    })
    if (!res.ok) throw new Error('Register failed')

    // After a successful signup, perform login so the backend session is established.
    await login(email, password)
  }

  async function logout(){
    await fetch('/api/logout', {method:'POST', credentials: 'include'})
    setUser(null)
    setSellerProfile(null)
    setIsAuthenticated(false)
  }

  return (
    <AuthContext.Provider value={{
      user,
      isAuthenticated,
      sellerProfile,
      isSeller: Boolean(sellerProfile),
      refreshSellerProfile: loadSellerProfile,
      login,
      register,
      logout
    }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth(){ return useContext(AuthContext) }
