package com.example.projetointegrador

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.projetointegrador.databinding.FragmentTelaAgendaBinding

class TelaAgendaFragment : Fragment() {

    private var _binding: FragmentTelaAgendaBinding? = null
    private val binding get() = _binding!!

    private var selectedDate: String? = null
    private var selectedTime: String? = null
    private var selectedService: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTelaAgendaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun initListeners() {
        // Seleção de data
        binding.cardCalendar.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = "$dayOfMonth/${month + 1}/$year"
            Toast.makeText(requireContext(), "Data escolhida: $selectedDate", Toast.LENGTH_SHORT).show()
        }

        // Seleção de horários
        setupTimeButtons()

        // Seleção de serviços
        setupServiceButtons()

        // Botão de voltar
        binding.btnArrowBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Botão confirmar
        binding.btnConfirm.setOnClickListener {
            if (selectedDate != null && selectedTime != null && selectedService != null) {
                Toast.makeText(
                    requireContext(),
                    "Agendado: $selectedDate às $selectedTime ($selectedService)",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                findNavController().navigate(R.id.action_telaAgendaFragment_to_telaAgenda2Fragment)
            }
        }
    }

    // Seleção de horários
    private fun setupTimeButtons() {
        val timeButtons = listOf(binding.btnTime1, binding.btnTime2, binding.btnTime3)

        for (btn in timeButtons) {
            btn.setOnClickListener {
                resetTimeButtons(timeButtons)
                btn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purple_200))
                btn.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                selectedTime = btn.text.toString()
                binding.tvSelectedTime.text = "$selectedTime*"
            }
        }
    }

    private fun resetTimeButtons(buttons: List<TextView>) {
        for (btn in buttons) {
            btn.setBackgroundResource(R.drawable.border_calendar)
            btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_200))
        }
    }

    // Seleção de serviços
    private fun setupServiceButtons() {
        val serviceButtons = listOf(binding.btnFaxina, binding.btnHidraulica, binding.btnEletrica)

        for (btn in serviceButtons) {
            btn.setOnClickListener {
                resetServiceButtons(serviceButtons)
                btn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purple_200))
                btn.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                selectedService = btn.text.toString()
            }
        }
    }

    private fun resetServiceButtons(buttons: List<TextView>) {
        for (btn in buttons) {
            btn.setBackgroundResource(R.drawable.border_calendar)
            btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_200))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}