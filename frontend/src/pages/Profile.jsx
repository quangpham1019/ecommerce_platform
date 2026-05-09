import React, { useEffect, useState } from 'react'
import { useAuth } from '../contexts/AuthContext'

export default function Profile(){
  const { user, sellerProfile, isSeller, refreshSellerProfile } = useAuth()
  const [displayName, setDisplayName] = useState('')
  const [bio, setBio] = useState('')
  const [message, setMessage] = useState(null)
  const [error, setError] = useState(null)

  useEffect(()=>{
    if (sellerProfile) {
      setDisplayName(sellerProfile.displayName || '')
      setBio(sellerProfile.bio || '')
    }
  }, [sellerProfile])

  async function handleSubmit(event){
    event.preventDefault()
    setMessage(null)
    setError(null)

    const payload = { displayName, bio }
    const url = isSeller ? '/api/seller-profiles/me' : '/api/seller-profiles'
    const method = isSeller ? 'PATCH' : 'POST'

    try {
      const res = await fetch(url, {
        method,
        credentials: 'include',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(payload)
      })

      if (!res.ok) {
        const body = await res.json().catch(() => null)
        throw new Error(body?.message || 'Failed to save seller profile')
      }

      await refreshSellerProfile()
      setMessage(isSeller ? 'Seller profile updated.' : 'Seller profile created.' )
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <div>
      <h2 className="text-2xl font-semibold">Profile</h2>
      <p className="mt-4 text-sm text-gray-600">Signed in as <strong>{user?.email}</strong>.</p>

      <section className="mt-8 rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
        <h3 className="text-xl font-semibold">Seller Profile</h3>
        <p className="mt-2 text-sm text-gray-600">Create or update your seller profile so you can list products.</p>

        {isSeller && (
          <div className="mt-4 rounded-md bg-green-50 border border-green-200 p-4 text-sm text-green-700">
            Seller profile is active.
          </div>
        )}

        <form className="mt-6 space-y-4" onSubmit={handleSubmit}>
          <div>
            <label className="block text-sm font-medium text-gray-700">Store name</label>
            <input
              className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
              value={displayName}
              onChange={e => setDisplayName(e.target.value)}
              placeholder="My store name"
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700">Bio</label>
            <textarea
              className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
              value={bio}
              onChange={e => setBio(e.target.value)}
              placeholder="Tell buyers about your shop"
              rows={4}
            />
          </div>

          {error && <div className="text-sm text-red-600">{error}</div>}
          {message && <div className="text-sm text-green-600">{message}</div>}

          <button
            type="submit"
            className="inline-flex items-center justify-center rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
          >
            {isSeller ? 'Update Seller Profile' : 'Create Seller Profile'}
          </button>
        </form>
      </section>
    </div>
  )
}
