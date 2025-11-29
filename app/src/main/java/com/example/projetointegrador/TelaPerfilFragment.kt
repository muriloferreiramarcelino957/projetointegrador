package com.example.projetointegrador

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        super.onViewCreated(view, savedInstanceState)

        setupNavigationBar()
        setupListeners()

        val uid = args.uidPrestador

        carregarNomeDoUsuario(uid)
        carregarDadosDoPrestador(uid)
    }

    // -------------------------------------------------------------------------
    // NAVBAR E LISTENERS
    // -------------------------------------------------------------------------

    private fun setupNavigationBar() {
        TopNavigationBarHelper.setupNavigationBar(binding.root, this)
    }

    private fun setupListeners() {
        binding.btnArrowBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnAgendar.setOnClickListener {
            val action = TelaPerfilFragmentDirections
                .actionTelaPerfilFragmentToTelaAgendaFragment(args.uidPrestador)

            findNavController().navigate(action)
        }
    }


    // -------------------------------------------------------------------------
    // CARREGA O NOME EM /usuarios/{uid}
    // -------------------------------------------------------------------------

    private fun carregarNomeDoUsuario(uid: String) {
        FirebaseDatabase.getInstance().reference
            .child("usuarios")
            .child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    val nome = snap.child("nome").getValue(String::class.java)
                    binding.txtNome.text = nome ?: "Prestador"
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // -------------------------------------------------------------------------
    // CARREGA DADOS EM /prestadores/{uid}
    // -------------------------------------------------------------------------

    private fun carregarDadosDoPrestador(uid: String) {
        database = FirebaseDatabase.getInstance().reference
        val ref = database.child("prestadores").child(uid)

        listenerPrestador = object : ValueEventListener {

            override fun onDataChange(snap: DataSnapshot) {

                // ------------------- DESCRIÇÃO -------------------
                binding.txtDescricao.text =
                    snap.child("info_prestador/descricao")
                        .getValue(String::class.java)
                        ?: "Sem descrição cadastrada"

                // ------------------- NOTA -------------------
                val nota = snap.child("info_prestador/notaMedia")
                    .getValue(Double::class.java) ?: 0.0

                val notaFormatada = String.format("%.1f", nota)
                binding.txtRatingMediaValor.text = notaFormatada
                binding.txtRatingMediaValor.text = notaFormatada

                // ------------------- NÍVEL DE CADASTRO -------------------
                val nivel = snap.child("info_prestador/nivel_cadastro")
                    .getValue(String::class.java) ?: "bronze"

                binding.progressCadastro.progress = when (nivel.lowercase()) {
                    "bronze" -> 33
                    "prata" -> 66
                    "ouro" -> 100
                    else -> 10
                }

                // ------------------- DATA CADASTRO -------------------
                val dataBruta = snap.child("data_cadastro")
                    .getValue(String::class.java)

                if (!dataBruta.isNullOrEmpty()) {
                    binding.txtDesde.text = "Na AllService desde ${formatarData(dataBruta)}"
                }

                // ------------------- QUANTIDADE DE SERVIÇOS -------------------
                val qtd = snap.child("info_prestador/quantidade_de_servicos")
                    .getValue(Int::class.java) ?: 0

                binding.quantidadeServicos.text =
                    "Quantidade de serviços prestados: $qtd"

                // ------------------- SERVIÇOS OFERECIDOS -------------------
                val servicosNode = snap.child("servicos_oferecidos").children
                val ids = servicosNode.mapNotNull { it.key?.toIntOrNull() }

                if (ids.isEmpty()) {
                    binding.servicosTags.text = "Nenhum serviço"
                } else {
                    carregarDescricoesServicos(ids)
                }

                // ------------------- DISPONIBILIDADE -------------------
                val inicio = snap.child("disponibilidade/segunda/inicio")
                    .getValue(String::class.java)

                val fim = snap.child("disponibilidade/segunda/fim")
                    .getValue(String::class.java)

                binding.txtLocal.text =
                    if (inicio != null && fim != null)
                        "Disponível: $inicio — $fim"
                    else
                        "Disponibilidade não informada"
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        ref.addValueEventListener(listenerPrestador!!)
    }

    // -------------------------------------------------------------------------
    // BUSCA AS DESCRIÇÕES DOS SERVIÇOS EM /tipos_de_servico
    // -------------------------------------------------------------------------

    private fun carregarDescricoesServicos(ids: List<Int>) {

        val refTipos = FirebaseDatabase.getInstance().reference.child("tipos_de_servico")

        val descricoes = mutableListOf<String>()
        var carregados = 0

        ids.forEach { id ->
            refTipos.child(id.toString()).child("dscr_servico")
                .get()
                .addOnSuccessListener { dsnap ->
                    val desc = dsnap.getValue(String::class.java)
                    if (desc != null) descricoes.add(desc)
                }
                .addOnCompleteListener {
                    carregados++
                    if (carregados == ids.size) {
                        binding.servicosTags.text =
                            descricoes.joinToString(" | ")
                    }
                }
        }
    }

    // -------------------------------------------------------------------------
    // FORMATA DATA: yyyy-MM-dd → dd/MM/yyyy
    // -------------------------------------------------------------------------

    private fun formatarData(data: String): String = try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val out = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        out.format(parser.parse(data)!!)
    } catch (e: Exception) {
        data
    }

    // -------------------------------------------------------------------------
    // LIMPEZA DE LISTENER
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
