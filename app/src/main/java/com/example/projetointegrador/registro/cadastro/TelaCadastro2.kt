package com.example.projetointegrador.registro.cadastro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.projetointegrador.R
import com.example.projetointegrador.databinding.FlagPrestadorBinding
import com.example.projetointegrador.databinding.TelaDeCadastro2Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class TelaCadastro2 : Fragment() {

    private var _binding: TelaDeCadastro2Binding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private val args by navArgs<TelaCadastro2Args>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = TelaDeCadastro2Binding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        val user = args.user
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun initListeners(){
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.btnCadastrar.setOnClickListener {
            val cep = binding.editTextTextCEP.text.toString().trim()
            val tipoLogradouro = binding.editTextTipoLogradouro.text.toString().trim()
            val descLogradouro = binding.editTextTextDescricaoLogradouro.text.toString().trim()
            val numero = binding.editTextTextNumero.text.toString().trim()
            val bairro = binding.editTextTextBairro.text.toString().trim()
            val cidade = binding.editTextCidade.text.toString().trim()
            val estado = binding.editTextUF.text.toString().trim()

            if (cep.isEmpty() || tipoLogradouro.isEmpty() || descLogradouro.isEmpty() || numero.isEmpty() || bairro.isEmpty() || cidade.isEmpty() || estado.isEmpty()) {
                Toast.makeText(requireContext(), "Preencha todos os campos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = args.user
                user?.cep = cep
                user?.tipoLogradouro = tipoLogradouro
                user?.descLogradouro = descLogradouro
                user?.numero = numero




            val flag = FlagPrestadorBinding.inflate(layoutInflater)
            val overlay = flag.root
            overlay.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            val root = binding.root
            root.addView(overlay)

            flag.btnSim.setOnClickListener {
                findNavController().navigate(R.id.action_telaCadastro2_to_tipoDeServico1Fragment)
            }
            flag.btnNao.setOnClickListener {
                Toast.makeText(requireContext(), "Ir para tela principal", Toast.LENGTH_SHORT).show()
                root.removeView(overlay)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}