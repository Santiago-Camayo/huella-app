package com.example.huella.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.huella.data.Resultado

// HU-13: animales guardados como favoritos. Su estado se refresca con la API al entrar.
@Composable
fun FavoritosScreen(vm: AnimalesViewModel, abrir: (Long) -> Unit) {
    LaunchedEffect(Unit) { vm.cargarFavoritos() }

    Column(Modifier.fillMaxSize()) {
        BarraSuperior("Favoritos")
        when (val lista = vm.listaFavoritos) {
            null -> Cargando()
            is Resultado.Error -> ErrorConReintento(lista.mensaje, vm::cargarFavoritos)
            is Resultado.Ok -> {
                // Al quitar el corazón aquí, la tarjeta desaparece sin volver a pedir nada
                val visibles = lista.dato.filter { it.id in vm.favoritos }
                if (visibles.isEmpty()) {
                    Mensaje("♡", "Aún no tienes favoritos", "Toca el corazón en un animal para guardarlo.")
                } else {
                    LazyVerticalGrid(
                        GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(visibles, key = { it.id }) {
                            TarjetaAnimal(it, favorito = true, alternarFavorito = { vm.alternarFavorito(it.id) }) { abrir(it.id) }
                        }
                    }
                }
            }
        }
    }
}
