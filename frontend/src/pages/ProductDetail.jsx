import React from 'react'
import { useParams } from 'react-router-dom'

export default function ProductDetail(){
  const { id } = useParams()
  return (
    <div>
      <h2 className="text-2xl font-semibold">Product {id}</h2>
      <p className="mt-4">Product details will appear here.</p>
    </div>
  )
}
