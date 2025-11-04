package com.projetointegrador.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projetointegrador.databinding.FragmentTelaPrincipalBinding



class FragmentTelaPrincipal : Fragment() {

    private var _binding: FragmentTelaPrincipalBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTelaPrincipalBinding.inflate(inflater, container, false)

        setupAnunciosRecycler()
        setupSearchInput()

        return binding.root
    }

    private fun setupAnunciosRecycler() {
        // Lista de anúncios simulada
        val anuncios = listOf(
            Anuncio("Serviços Hidráulicos", "R$ 150,00", R.drawable.ic_anuncio1),
            Anuncio("Serviços de Jardinagem", "R$ 200,00", R.drawable.ic_anuncio2),
            Anuncio("Serviços Elétricos", "R$ 250,00", R.drawable.ic_anuncio3),
            Anuncio("Pintura Residencial", "R$ 180,00", R.drawable.ic_anuncio4)
        )

        // Configura o RecyclerView horizontal
        binding.recyclerAnuncios.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        binding.recyclerAnuncios.adapter = AnuncioAdapter(anuncios)
    }

    private fun setupSearchInput() {
        // Apenas exemplo — você pode adicionar lógica de busca real aqui depois
        binding.searchInput.setOnClickListener {
            // Exemplo: exibir uma mensagem ou abrir outra tela
            println("Campo de busca clicado!")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
