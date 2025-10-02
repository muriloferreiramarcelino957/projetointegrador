package com.example.projetointegrador.registro.cadastro

import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.projetointegrador.R
import com.example.projetointegrador.databinding.FragmentTipoDeServico1Binding
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.Locale

class TipoDeServico1Fragment : Fragment() {

    private var _binding: FragmentTipoDeServico1Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTipoDeServico1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()

        val tipos = listOf("Tipo de serviço 1", "Fáxina", "Hidraúlica", "Elétrica")
        binding.autoTipoServico.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, tipos)
        )
        binding.autoTipoServico.setOnClickListener { binding.autoTipoServico.showDropDown() }

        // Valor p/ hora: apenas 3 dígitos numéricos (XML já limita; reforço via filtro)
        binding.editValorHora.filters = arrayOf(InputFilter.LengthFilter(3))

        setupTimeInput(binding.inputHorario1)
        setupTimeInput(binding.inputHorario2)
        setupTimeInput(binding.inputHorario3)
    }

    private fun setupTimeInput(editText: android.widget.EditText) {
        val openPicker = View.OnClickListener {
            val picker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(8)
                .setMinute(0)
                .setTitleText("Selecione um horário")
                .build()

            picker.addOnPositiveButtonClickListener {
                val h = picker.hour
                val m = picker.minute
                editText.setText(String.format(Locale.getDefault(), "%02d:%02d", h, m))
            }
            picker.show(parentFragmentManager, editText.id.toString())
        }
        editText.setOnClickListener(openPicker)
        editText.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) openPicker.onClick(editText) }
    }

    private fun checkInputs(): Int {
        if (binding.autoTipoServico.text.toString().isEmpty()){
            binding.autoTipoServico.error = "Insira um tipo de serviço"
            return 1
        } else{
            binding.autoTipoServico.error = null
        }
        if (binding.editValorHora.text.toString().isEmpty()){
            binding.editValorHora.error = "Insira um valor para o serviço prestado"
            return 1
        } else{
            binding.editValorHora.error = null
        }
        if (binding.inputHorario1.text.toString().isEmpty()){
            binding.inputHorario1.error = "Insira um horário"
            return 1
        } else{
            binding.inputHorario1.error = null
        }
        if (binding.inputHorario2.text.toString().isEmpty()){
            binding.inputHorario2.error = "Insira um horário"
            return 1
        } else{
            binding.inputHorario2.error = null
        }
        if (binding.inputHorario3.text.toString().isEmpty()){
            binding.inputHorario3.error = "Insira um horário"
            return 1
        } else{
            binding.inputHorario3.error = null
        }

        val horarios = setOf(
            binding.inputHorario1.text.toString().trim(),
            binding.inputHorario2.text.toString().trim(),
            binding.inputHorario3.text.toString().trim())
        if (horarios.size < 3){
            binding.inputHorario1.error = "Insira um horário distinto"
            binding.inputHorario2.error = "Insira um horário distinto"
            binding.inputHorario3.error = "Insira um horário distinto"
            return 2
        } else{
            binding.inputHorario1.error = null
            binding.inputHorario2.error = null
            binding.inputHorario3.error = null
        }
        return 0
    }

    private fun initListeners(){
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.btnProximo.setOnClickListener {
            when (checkInputs()) {
                0 -> findNavController().navigate(R.id.action_tipoDeServico1Fragment_to_tipoDeServico2)
                1 -> Toast.makeText(requireContext(), "Existem campos vazios", Toast.LENGTH_SHORT).show()
                2 -> Toast.makeText(requireContext(), "Insira horários distintos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}