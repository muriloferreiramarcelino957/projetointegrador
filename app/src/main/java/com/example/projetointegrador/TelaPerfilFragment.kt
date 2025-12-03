package com.example.projetointegrador

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.projetointegrador.databinding.TelaDePerfilBinding
import com.example.projetointegrador.navigation.TopNavigationBarHelper
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Locale

class TelaPerfilFragment : Fragment() {

    private var _binding: TelaDePerfilBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<TelaPerfilFragmentArgs>()
    private lateinit var database: DatabaseReference
    private var listenerPrestador: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TelaDePerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        TopNavigationBarHelper.setupNavigationBar(binding.root, this)
        setupListeners()

        val uid = args.uidPrestador

        carregarDadosUsuario(uid)
        carregarDadosPrestador(uid)
    }

    // -------------------------------------------------------------------------
    // BOTÕES
    // -------------------------------------------------------------------------

    private fun setupListeners() {
        binding.btnArrowBack.setOnClickListener { findNavController().navigateUp() }

        binding.btnAgendar.setOnClickListener {
            val action = TelaPerfilFragmentDirections
                .actionTelaPerfilFragmentToTelaAgendaFragment(args.uidPrestador)
            findNavController().navigate(action)
        }
    }

    // -------------------------------------------------------------------------
    // USUÁRIO (NOME, EMAIL, TELEFONE)
    // /usuarios/UID
    // -------------------------------------------------------------------------

    private fun carregarDadosUsuario(uid: String) {
        val ref = FirebaseDatabase.getInstance().reference
            .child("usuarios")
            .child(uid)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snap: DataSnapshot) {

                val nome = snap.child("nome").getValue(String::class.java) ?: "Prestador"
                val email = snap.child("email").getValue(String::class.java) ?: "Não informado"
                val telefone = snap.child("telefone").getValue(String::class.java) ?: "Não informado"
                val facebook = snap.child("facebook").getValue(String::class.java) ?: "Não informado"

                // Nome (campo grande)
                binding.txtNome.text = nome

                // Demais informações
                binding.txtEmail.text = "✓ Endereço de e-mail: $email"
                binding.txtTelefone.text = "✓ Número de telefone: $telefone"
                binding.txtFacebook.text = "✓ Facebook: $facebook"
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // -------------------------------------------------------------------------
    // PRESTADOR (DESCRIÇÃO, NOTA, NÍVEL, SERVIÇOS ...)
    // /prestadores/UID
    // -------------------------------------------------------------------------

    private fun carregarDadosPrestador(uid: String) {

        database = FirebaseDatabase.getInstance().reference
        val refPrestador = database.child("prestadores").child(uid)

        listenerPrestador = object : ValueEventListener {

            override fun onDataChange(snap: DataSnapshot) {

                // DESCRIÇÃO
                binding.txtDescricao.text =
                    snap.child("info_prestador/descricao")
                        .getValue(String::class.java)
                        ?: "Sem descrição cadastrada"

                // NOTA
                val nota = snap.child("info_prestador/notaMedia")
                    .getValue(Double::class.java) ?: 0.0
                binding.txtRatingMediaValor.text = String.format("%.1f", nota)

                // NÍVEL
                val nivel = snap.child("info_prestador/nivel_cadastro")
                    .getValue(String::class.java)?.lowercase() ?: "bronze"

                val drawable = when (nivel) {
                    "bronze" -> R.drawable.bg_progress_bronze
                    "prata" -> R.drawable.bg_progress_prata
                    "ouro" -> R.drawable.bg_progress_ouro
                    else -> R.drawable.bg_progress_bronze
                }

                binding.progressCadastro.progressDrawable =
                    ContextCompat.getDrawable(requireContext(), drawable)

                binding.progressCadastro.progress = when (nivel) {
                    "bronze" -> 33
                    "prata" -> 66
                    "ouro" -> 100
                    else -> 10
                }

                // DATA DE CADASTRO
                val dataBruta = snap.child("data_cadastro").getValue(String::class.java)
                if (!dataBruta.isNullOrEmpty()) {
                    binding.txtDesde.text = "Na AllService desde ${formatarData(dataBruta)}"
                }

                // QUANTIDADE DE SERVIÇOS
                val qtd = snap.child("info_prestador/quantidade_de_servicos")
                    .getValue(Int::class.java) ?: 0
                binding.quantidadeServicos.text =
                    "Quantidade de serviços prestados: $qtd"

                // SERVIÇOS OFERECIDOS (IDs)
                val ids = snap.child("servicos_oferecidos").children
                    .mapNotNull { it.key?.toIntOrNull() }

                if (ids.isEmpty()) {
                    binding.servicosTags.text = "Nenhum serviço"
                } else {
                    carregarDescricoesServicos(ids)
                }

                // DISPONIBILIDADE
                val inicio =
                    snap.child("disponibilidade/segunda/inicio").getValue(String::class.java)
                val fim =
                    snap.child("disponibilidade/segunda/fim").getValue(String::class.java)

                binding.txtLocal.text = if (inicio != null && fim != null)
                    "Disponível: $inicio — $fim"
                else
                    "Disponibilidade não informada"

            }

            override fun onCancelled(error: DatabaseError) {}
        }

        refPrestador.addValueEventListener(listenerPrestador!!)
    }

    // -------------------------------------------------------------------------
    // SERVIÇOS OFERECIDOS (DESCRIÇÕES)
    // -------------------------------------------------------------------------

    private fun carregarDescricoesServicos(ids: List<Int>) {

        val refTipos = FirebaseDatabase.getInstance().reference.child("tipos_de_servico")

        val descricoes = mutableListOf<String>()
        var carregados = 0

        ids.forEach { id ->

            refTipos.child(id.toString()).child("dscr_servico")
                .get()
                .addOnSuccessListener { snap ->
                    snap.getValue(String::class.java)?.let { descricoes.add(it) }
                }
                .addOnCompleteListener {
                    carregados++

                    if (carregados == ids.size) {
                        binding.servicosTags.text = descricoes.joinToString(" | ")
                    }
                }
        }
    }

    // -------------------------------------------------------------------------
    // FORMATAR DATA
    // -------------------------------------------------------------------------

    private fun formatarData(data: String): String = try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val out = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        out.format(parser.parse(data)!!)
    } catch (e: Exception) {
        data
    }

    // -------------------------------------------------------------------------
    // LIMPAR LISTENERS
    // -------------------------------------------------------------------------

    override fun onDestroyView() {
        super.onDestroyView()
        listenerPrestador?.let {
            database.child("prestadores")
                .child(args.uidPrestador)
                .removeEventListener(it)
        }
        _binding = null
    }
}
