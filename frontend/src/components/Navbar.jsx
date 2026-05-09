import React from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { useCart } from '../contexts/CartContext'

export default function Navbar(){
  const { isAuthenticated, user, sellerProfile, logout } = useAuth()
  const { items } = useCart()
  const count = items.reduce((s,i)=>s + (i.qty||0), 0)

  return (
    <header className="bg-white shadow">
      <div className="max-w-6xl mx-auto py-4 px-6 flex justify-between items-center">
        <Link to="/" className="text-lg font-semibold">Marketplace</Link>
        <nav className="flex items-center gap-4">
          <Link to="/">Browse</Link>
          {isAuthenticated && sellerProfile && <Link to="/seller">Seller</Link>}
          <Link to="/cart">Cart ({count})</Link>
          {isAuthenticated ? (
            <>
              <Link to="/profile">{user?.email}</Link>
              <button className="text-sm text-gray-600" onClick={logout}>Logout</button>
            </>
          ) : (
            <>
              <Link to="/login">Login</Link>
              <Link to="/register" className="ml-2">Register</Link>
            </>
          )}
        </nav>
      </div>
    </header>
  )
}
