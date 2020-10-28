package com.br.clientehabitual;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.br.clientehabitual.adapters.ClienteAdapter;
import com.br.clientehabitual.banco.daos.ClienteDAO;
import com.br.clientehabitual.banco.daos.InadimplenciaDAO;
import com.br.clientehabitual.models.Cliente;
import com.br.clientehabitual.models.Inadimplencia;
import com.br.clientehabitual.notificacao.JobServiceNotification;
import com.br.clientehabitual.util.Conversoes;
import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.pattern.MaskPattern;
import com.github.rtoshiro.util.format.text.MaskTextWatcher;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private EditText pesquisa, valorpopup, dataPagamento;
    private Button add;
    private CheckBox registrarProdutos;
    private ListView lista;
    private ArrayList<Cliente> clientes;
    private ClienteAdapter adapter;
    private Conversoes conversoes;
    private ClienteDAO clienteDAO = new ClienteDAO(this);
    private InadimplenciaDAO inadimplenciaDAO = new InadimplenciaDAO(this);
    private DecimalFormat df = new DecimalFormat("#0.00");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iniciarJobScheduler();
        gerarListaClientes();

        FloatingActionButton novaInadimplencia = findViewById(R.id.btn_nova_inadimplencia);
        novaInadimplencia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupNovoCliente();
            }
        });

    }
    public void iniciarJobScheduler(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            ComponentName componentName = new ComponentName(this, JobServiceNotification.class);
            PersistableBundle persistableBundle = new PersistableBundle();
            JobInfo.Builder builder = new JobInfo.Builder(1, componentName)
                    .setBackoffCriteria(43200000, JobInfo.BACKOFF_POLICY_LINEAR)
                    .setExtras(persistableBundle)
                    .setPersisted(true)
                    .setRequiresCharging(false)
                    .setRequiresDeviceIdle(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                builder.setPeriodic(43200000, 43200000);
            } else {
                builder.setPeriodic(43200000);
            }
            JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            jobScheduler.schedule(builder.build());
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        gerarListaClientes();
    }
    public void gerarListaClientes(){
        clientes = ordenarAtrazado();

        lista = findViewById(R.id.mainLista);
        adapter = new ClienteAdapter(this,clientes);
        lista.setTextFilterEnabled(true);
        lista.setAdapter(adapter);

        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Inadimplencia inadimplencia = inadimplenciaDAO.getInadimpleciaCliente(clientes.get(position));
                if (inadimplencia != null){
                    Calendar calendar = Calendar.getInstance();
                    if (inadimplencia.getDataFim() ==  null){
                        Intent intent = new Intent(MainActivity.this, ClienteActivity.class);
                        intent.putExtra("id",Long.toString(clientes.get(position).getId()));
                        startActivity(intent);
                    }
                    else if (calendar.getTime().after(inadimplencia.getDataFim().getTime()) && inadimplencia.isQuitada() == false){
                        if (clientes.get(position).getEmail().length() < 1){
                            Toast.makeText(getApplicationContext(),"Nenhum E-mail registrado para cobrança, Atualize os dados do cliente ou digite manualmente!",Toast.LENGTH_LONG).show();
                        }
                            emailConfirm(inadimplencia);
                    } else {
                        Intent intent = new Intent(MainActivity.this, ClienteActivity.class);
                        intent.putExtra("id",Long.toString(clientes.get(position).getId()));
                        startActivity(intent);
                    }
                } else{
                        Intent intent = new Intent(MainActivity.this, ClienteActivity.class);
                        intent.putExtra("id",Long.toString(clientes.get(position).getId()));
                        startActivity(intent);
                }
            }
        });

        pesquisa = findViewById(R.id.mainPesquisa);
        pesquisa.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!clientes.isEmpty() && pesquisa.length() > 0){
                    adapter = new ClienteAdapter(MainActivity.this, filtrar(clientes));
                    lista.setAdapter(adapter);
                } else {
                    adapter = new ClienteAdapter(MainActivity.this,clientes);
                    lista.setAdapter(adapter);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    public void popupNovoCliente(){
        AlertDialog.Builder dialogBuilder;
        final AlertDialog dialog;
        dialogBuilder = new AlertDialog.Builder(this);
        final  View popupClienteView = getLayoutInflater().inflate(R.layout.popup_cliente,null);
        dialogBuilder.setView(popupClienteView);
        dialog = dialogBuilder.create();
        dialog.setTitle("Nova Inadimplêcia!");
        dialog.show();

        final EditText nome = (EditText)popupClienteView.findViewById(R.id.popupNome);
        final EditText email = (EditText)popupClienteView.findViewById(R.id.popupEmail);

        valorpopup = (EditText)popupClienteView.findViewById(R.id.popup_cliente_valor);
        dataPagamento = (EditText)popupClienteView.findViewById(R.id.popup_data_pagamento);

        final ImageView sifrao = popupClienteView.findViewById(R.id.sifrao);
        final ImageView calendar = popupClienteView.findViewById(R.id.calendar_icon);

        add = popupClienteView.findViewById(R.id.btnProximo);

        SimpleMaskFormatter maskData = new SimpleMaskFormatter("[0-3][0-9]/[0-1][0-9]/[0-9][0-9][0-9][0-9]");
        MaskPattern maskPattern1 = new MaskPattern("[0-1]");
        MaskPattern maskPattern2 = new MaskPattern("[0-3]");
        MaskPattern maskPattern3 = new MaskPattern("[0-9]");
        maskData.registerPattern(maskPattern1);
        maskData.registerPattern(maskPattern2);
        maskData.registerPattern(maskPattern3);
        MaskTextWatcher maskTextWatcher = new MaskTextWatcher(dataPagamento, maskData);
        dataPagamento.addTextChangedListener(maskTextWatcher);

        registrarProdutos = (CheckBox)popupClienteView.findViewById(R.id.check_listar);
        registrarProdutos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (registrarProdutos.isChecked()) {
                    valorpopup.setVisibility(View.INVISIBLE);
                    dataPagamento.setVisibility(View.INVISIBLE);
                    sifrao.setVisibility(View.INVISIBLE);
                    calendar.setVisibility(View.INVISIBLE);
                    add.setText("PROXIMO");
                    dataPagamento.setText("");
                    valorpopup.setText("");
                    dialog.setTitle("Novo Cliente!");
                } else {
                    valorpopup.setVisibility(View.VISIBLE);
                    dataPagamento.setVisibility(View.VISIBLE);
                    sifrao.setVisibility(View.VISIBLE);
                    calendar.setVisibility(View.VISIBLE);
                    add.setText("SALVAR");
                    dialog.setTitle("Nova Inadimplêcia!");
                }

            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cliente cliente = new Cliente(0, nome.getText().toString().trim(),
                        email.getText().toString().trim());
                if (add.getText().toString().equals("PROXIMO")) {
                    if (cliente.getNome().length() == 0) {
                        Toast.makeText(getApplicationContext(), "Adicione o nome do clente!", Toast.LENGTH_SHORT).show();
                    } else {
                        dialog.dismiss();
                        Intent intent = new Intent(MainActivity.this, ClienteActivity.class);
                        intent.putExtra("id", Long.toString(clienteDAO.cadastrarCliente(cliente).getId()));
                        startActivity(intent);
                    }
                } else {
                    /* ADICIONANDO APENAS O VALOR DA INADIMPLÊCIA*/
                    conversoes = new Conversoes();
                    Calendar data = Calendar.getInstance();
                    Inadimplencia inadimplencia = new Inadimplencia(0, data, conversoes.stringToCalendar(dataPagamento.getText().toString().trim().replaceAll("/", "-")),
                            cliente, false, 0);
                    if (valorpopup.getText().toString().trim().length() != 0){
                        if (valorpopup.getText().toString().trim().equals(".")){
                            Toast.makeText(getApplicationContext(), "Entre com um valor valido!", Toast.LENGTH_SHORT).show();
                        } else{
                            inadimplencia.setTotal(Float.parseFloat(valorpopup.getText().toString().trim()));
                        }
                    }
                    if (cliente.getNome().length() == 0 || inadimplencia.getDataFim() == null || inadimplencia.getTotal() <= 0) {
                        Toast.makeText(getApplicationContext(), "Preencha os campos, e tente novamente!", Toast.LENGTH_SHORT).show();
                    } else {
                        dialog.dismiss();
                        cliente = clienteDAO.cadastrarCliente(cliente);
                        inadimplencia.setCliente(cliente);
                        inadimplencia = inadimplenciaDAO.newInadimplencia(inadimplencia);
                        inadimplenciaDAO.setDataPagamentoInadimplencia(inadimplencia);
                        Toast.makeText(getApplicationContext(), "Inadimplência registrada com sucesso!", Toast.LENGTH_LONG).show();
                    }
                }
                gerarListaClientes();
            }
        });
    }
    public void emailConfirm(final Inadimplencia inadimplencia){
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage("Enviar E-mail para: "+inadimplencia.getCliente().getNome()+" ?")
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        conversoes = new Conversoes();
                        String totalPreco = df.format(inadimplencia.getTotal()).replaceAll(Pattern.quote("."), ",");
                        String data = conversoes.calendarToString(inadimplencia.getDataFim()).replaceAll("-","/");
                        String assunto = "Referente a inadimplência";
                        String mensagem = "Caro cliente: "+ inadimplencia.getCliente().getNome() + " por meio desse E-mail " +
                                "estamos entrando em contato com você para avisar sobre sua divida de: R$ " +
                                totalPreco +" com data de pagamento expirada no dia: " + data +
                                " pedimos que compareça ao nosso estabelecimento para quitar sua divida. Atenciosamente: ";

                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setData(Uri.parse("mailto:"));
                        intent.setType("text/plain");

                        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{inadimplencia.getCliente().getEmail()});
                        intent.putExtra(Intent.EXTRA_SUBJECT, assunto);
                        intent.putExtra(Intent.EXTRA_TEXT, mensagem);

                        try {
                            startActivity(Intent.createChooser(intent,"Enviar E-mail"));
                        }catch (Exception e){
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }

                })
                .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent(MainActivity.this, ClienteActivity.class);
                        intent.putExtra("id",Long.toString(inadimplencia.getCliente().getId()));
                        startActivity(intent);
                    }
                }).show();
    }
    public ArrayList<Cliente> filtrar(ArrayList<Cliente> clientes){
        String pesquisaString = pesquisa.getText().toString();
        ArrayList<Cliente> clientesFiltrados = new ArrayList<>();
        for (Cliente c : clientes){
            if (pesquisaString.length() <= c.getNome().length()){
                if (pesquisaString.equalsIgnoreCase((String) c.getNome().subSequence(0, pesquisaString.length()))){
                    clientesFiltrados.add(c);
                }
            }
        }
        return clientesFiltrados;
    }
    public ArrayList<Cliente> ordenarAtrazado(){
        clientes = clienteDAO.listaClientes();
        ArrayList<Cliente> organizado = new ArrayList<>();
        ArrayList<Cliente> emDia = new ArrayList<>();
        for (Cliente c : clientes){
            Inadimplencia inadimplencia = inadimplenciaDAO.getInadimpleciaCliente(c);
            if (inadimplencia != null){
                if (inadimplencia.isQuitada()){
                    emDia.add(c);
                } else{
                    if(inadimplencia.getDataFim() != null){
                        Calendar calendar = Calendar.getInstance();
                        if (calendar.getTime().after(inadimplencia.getDataFim().getTime())){
                            organizado.add(c);
                        } else{
                            emDia.add(c);
                        }
                    }
                }
            } else{
                emDia.add(c);
            }
        }
        organizado.addAll(emDia);
        return organizado;
    }
}
