package com.eurekabank.api.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.eurekabank.api.R
import com.eurekabank.api.data.models.Movimiento

class MovimientosAdapter(
    private val movimientos: List<Movimiento>
) : RecyclerView.Adapter<MovimientosAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivTipo: ImageView = view.findViewById(R.id.ivTipo)
        val tvTipo: TextView = view.findViewById(R.id.tvTipo)
        val tvFecha: TextView = view.findViewById(R.id.tvFecha)
        val tvAccion: TextView = view.findViewById(R.id.tvAccion)
        val tvImporte: TextView = view.findViewById(R.id.tvImporte)
        val tvSaldo: TextView = view.findViewById(R.id.tvSaldo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movimiento, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val movimiento = movimientos[position]

        holder.tvTipo.text = movimiento.tipo
        holder.tvFecha.text = movimiento.fecha.substring(0, 10)
        holder.tvAccion.text = movimiento.accion

        val signo = if (movimiento.accion == "INGRESO") "+" else "-"
        holder.tvImporte.text = "$signo${movimiento.importe}"

        if (movimiento.accion == "INGRESO") {
            holder.tvImporte.setTextColor(Color.parseColor("#4CAF50"))
            holder.tvAccion.setTextColor(Color.parseColor("#4CAF50"))
            holder.ivTipo.setImageResource(android.R.drawable.ic_menu_add)
        } else {
            holder.tvImporte.setTextColor(Color.parseColor("#F44336"))
            holder.tvAccion.setTextColor(Color.parseColor("#F44336"))
            holder.ivTipo.setImageResource(android.R.drawable.ic_menu_delete)
        }

        holder.tvSaldo.text = "Saldo: ${movimiento.importe}"
    }

    override fun getItemCount(): Int = movimientos.size
}
