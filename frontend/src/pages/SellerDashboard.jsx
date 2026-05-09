import React from 'react'
import { useAuth } from '../contexts/AuthContext'
import { Link } from 'react-router-dom'

export default function SellerDashboard(){
  const { isAuthenticated, isSeller } = useAuth()

  if (!isAuthenticated) {
    return (
      <div className="min-h-[60vh] flex items-center justify-center">
        <div className="text-center">
          <h2 className="text-2xl font-semibold mb-2">Sign in to access Seller Dashboard</h2>
          <p className="mb-4 text-sm text-gray-600">You must be signed in to manage your seller products and orders.</p>
          <Link to="/login" className="bg-blue-600 text-white px-4 py-2 rounded">Sign in</Link>
        </div>
      </div>
    )
  }

  if (!isSeller) {
    return (
      <div className="min-h-[60vh] flex items-center justify-center">
        <div className="text-center">
          <h2 className="text-2xl font-semibold mb-2">You don't have a seller profile yet</h2>
          <p className="mb-4 text-sm text-gray-600">Create a seller profile to list products and manage orders.</p>
          <Link to="/profile" className="bg-green-600 text-white px-4 py-2 rounded">Create seller profile</Link>
        </div>
      </div>
    )
  }

  return (
    <div>
      <h2 className="text-2xl font-semibold">Seller Dashboard</h2>
      <p className="mt-4">Manage your products and orders here.</p>
    </div>
  )
}
