import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static List<Telefone> telefones = new ArrayList<>();
    static Scanner scanner = new Scanner(System.in);
    public static void main(String[] args) {

        int opcao;


        do {
            System.out.println("##################");
            System.out.println("##### AGENDA #####");
            System.out.println("##################\n");

            List<Contato> contatos = carregarContatos();


            System.out.println(">>>> Menu <<<<");
            System.out.println("1 - Adicionar Contato");
            System.out.println("2 - Remover Contato");
            System.out.println("3 - Editar Contato");
            System.out.println("4 - Sair");

            opcao = scanner.nextInt();

            switch (opcao) {
                case 1 -> adicionarContato(contatos,scanner);
                case 2 -> removerContato(contatos,scanner);
                case 3 -> editarContato(contatos,scanner);
                case 4 -> System.out.println("Saindo...");
                default -> System.out.println("Digite uma opção válida!");
            }
        } while (opcao != 4);
    }


    private static void adicionarContato(List<Contato> contatos,Scanner scanner) {
            List<Telefone> telefones = new ArrayList<>();
            scanner.nextLine();
            System.out.println("Digite o nome do contato: ");
            String nome = scanner.nextLine();
            System.out.println("Digite o sobrenome do contato: ");
            String sobrenome = scanner.nextLine();


            do {
                System.out.println("Informe o DDD do telefone: ");
                String ddd = scanner.next();
                System.out.println("Informe o número do telefone: ");
                Long numero = scanner.nextLong();

                while (telefoneJaCadastrado(contatos, ddd, numero)) {
                    System.out.println("Este telefone já está cadastrado em outro contato. Por favor, informe outro número.");
                    System.out.println("Informe o DDD do telefone: ");
                    ddd = scanner.next();
                    System.out.println("Informe o número do telefone: ");
                    numero = scanner.nextLong();
                }

                Telefone telefone = new Telefone(gerarId(contatos), ddd, numero);
                telefones.add(telefone);

                System.out.println("Deseja adicionar outro telefone? (S/N): ");
                String resposta = scanner.next();
                if (!resposta.equalsIgnoreCase("S")) {
                    break;
                }
            } while (true);

            Long id = gerarId(contatos);
            Contato contato = new Contato(id, nome, sobrenome);
            contato.setTelefones(telefones);
            contatos.add(contato);

            salvarContato(contato);
    }

    public static void salvarContato(Contato contato){
        try (PrintWriter writer = new PrintWriter(new FileWriter("contatos.txt", true))) {
            writer.print(contato.getId() + "," + contato.getNome() + "," + contato.getSobreNome() + ",");

            List<Telefone> telefones = contato.getTelefones();
            for (int i = 0; i < telefones.size(); i++) {
                Telefone telefone = telefones.get(i);
                writer.print(telefone.getDdd() + "-" + telefone.getNumero());

                if (i < telefones.size() - 1) {
                    writer.print(";");
                }
            }
            writer.println();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static List<Contato> carregarContatos(){
        List<Contato> contatos = new ArrayList<>();

        try(Scanner scanner = new Scanner(new File("contatos.txt"))) {
            System.out.println("ID | Nome         | Telefones");
            while (scanner.hasNextLine()){
                String[] partes = scanner.nextLine().split(",");
                if (partes.length >3) {
                    Long id = Long.parseLong(partes[0]);
                    String nome = partes[1];
                    String sobrenome = partes[2];
                    String[] telefonesStr = partes[3].split(";");


                    Contato contato = new Contato(id,nome,sobrenome);
                    List<Telefone> telefones = new ArrayList<>();

                    for (String telefoneStr : telefonesStr) {
                        String[] telefoneParts = telefoneStr.split("-");
                        String ddd = telefoneParts[0];
                        Long numero = Long.parseLong(telefoneParts[1]);
                        Telefone telefone = new Telefone(id, ddd, numero);
                        telefones.add(telefone);
                    }

                    contato.setTelefones(telefones);
                    contatos.add(contato);
                    System.out.println(id+ "  | " + nome+ " " + sobrenome+ " | " +partes[3]);
                }else {
                    System.err.println("Formato de contato inválido no arquivo.");
                }
            }
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }
        System.out.println("");
        return contatos;
    }

    private static void removerContato(List<Contato> contatos, Scanner scanner) {
        System.out.println("Digite o ID do contato que deseja remover:");
        Long id = scanner.nextLong();

        boolean removido = false;

        for (Contato contato : contatos) {
            if (contato.getId().equals(id)) {
                contatos.remove(contato);
                removido = true;
                removerContatoArquivo(contato);
                System.out.println("Contato removido com sucesso.");
                break;
            }

        }
        if (!removido) {
            System.out.println("Contato não encontrado.");
        }

    }

    private static void removerContatoArquivo(Contato contato) {
        try (BufferedReader reader = new BufferedReader(new FileReader("contatos.txt"));
             BufferedWriter writer = new BufferedWriter(new FileWriter("contatos_temp.txt"))) {

            String linha;
            while ((linha = reader.readLine()) != null) {
                if (!linha.startsWith(contato.getId() + ",")) {
                    writer.write(linha);
                    writer.newLine();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        Path originalPath = Paths.get("contatos.txt");
        Path tempPath = Paths.get("contatos_temp.txt");
        try {
            Files.move(tempPath, originalPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erro ao substituir o arquivo. Certifique-se de que nenhum outro programa está usando o arquivo.");
        }
        rebalancearId();
    }

    private static void editarContato(List<Contato> contatos, Scanner scanner) {
        System.out.println("Digite o ID do contato que deseja editar:");
        Long id = scanner.nextLong();

        Contato contatoSelecionado = null;


        for (Contato contato : contatos) {
            if (contato.getId().equals(id)) {
                contatoSelecionado = contato;
                break;
            }
        }

        if (contatoSelecionado == null) {
            System.out.println("Contato não encontrado.");
            return;
        }

        exibirInformacoesContato(contatoSelecionado);

                System.out.println("O que deseja alterar no contato? Escolha a opção: ");
                System.out.println("1 - Nome");
                System.out.println("2 - Sobrenome");
                System.out.println("3 - Telefone");
                int opcao = scanner.nextInt();

                switch (opcao) {
                    case 1:
                        System.out.println("Digite o novo nome: ");
                        String novoNome = scanner.next();
                        contatoSelecionado.setNome(novoNome);
                        break;
                    case 2:
                        System.out.println("Digite o novo sobrenome: ");
                        String novoSobrenome = scanner.next();
                        contatoSelecionado.setSobreNome(novoSobrenome);
                        break;
                    case 3:
                        editarTelefone(contatoSelecionado, scanner);
                        break;
                    default:
                        System.out.println("Opção inválida.");
                }

                editarContatoArquivo(contatoSelecionado);
                System.out.println("Contato editado com sucesso.");

    }



    private static void exibirInformacoesContato(Contato contato) {
        System.out.println("Informações atuais do contato:");
        System.out.println("Id: " + contato.getId());
        System.out.println("Nome: " + contato.getNome());
        System.out.println("Sobrenome: " + contato.getSobreNome());

        List<Telefone> telefones = contato.getTelefones();
        if (telefones != null && !telefones.isEmpty()) {
            System.out.println("Telefones:");
            for (Telefone telefone : telefones) {
                System.out.println("  DDD: " + telefone.getDdd() + ", Número: " + telefone.getNumero());
            }
        } else {
            System.out.println("Nenhum telefone cadastrado para este contato.");
        }
    }

    private static void editarContatoArquivo(Contato contato) {
        try (BufferedReader reader = new BufferedReader(new FileReader("contatos.txt"));
             BufferedWriter writer = new BufferedWriter(new FileWriter("contatos_temp.txt"))) {

            String linha;
            while ((linha = reader.readLine()) != null) {
                if (linha.startsWith(contato.getId() + ",")) {
                    writer.write(contato.getId() + "," + contato.getNome() + "," + contato.getSobreNome() + ",");

                    List<Telefone> telefones = contato.getTelefones();
                    for (int i = 0; i < telefones.size(); i++) {
                        Telefone telefone = telefones.get(i);
                        writer.write(telefone.getDdd() + "-" + telefone.getNumero());

                        if (i < telefones.size() - 1) {
                            writer.write(",");
                        }
                    }

                    writer.newLine();
                } else {
                    writer.write(linha);
                    writer.newLine();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        Path originalPath = Paths.get("contatos.txt");
        Path tempPath = Paths.get("contatos_temp.txt");
        try {
            Files.move(tempPath, originalPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erro ao substituir o arquivo. Certifique-se de que nenhum outro programa está usando o arquivo.");
        }
    }

    private static void editarTelefone(Contato contato, Scanner scanner) {
        System.out.println("Digite o DDD do telefone que deseja editar:");
        String dddAntigo = scanner.next();

        System.out.println("Digite o número do telefone que deseja editar:");
        Long numeroAntigo = scanner.nextLong();

        List<Telefone> telefones = contato.getTelefones();
        Telefone telefoneParaEditar = null;

        for (Telefone telefone : telefones) {
            if (telefone.getDdd().equals(dddAntigo) && telefone.getNumero().equals(numeroAntigo)) {
                telefoneParaEditar = telefone;
                break;
            }
        }

        if (telefoneParaEditar == null) {
            System.out.println("Telefone não encontrado.");
        } else {
            telefones.remove(telefoneParaEditar);

            System.out.println("Digite o novo DDD do telefone:");
            String novoDdd = scanner.next();
            System.out.println("Digite o novo número do telefone:");
            Long novoNumero = scanner.nextLong();

            telefones.add(new Telefone(contato.getId(), novoDdd, novoNumero));
            System.out.println("Telefone editado com sucesso.");
        }
    }

    private static Long gerarId(List<Contato> contatos) {
        if (contatos.isEmpty()){
            return 1L;
        } Long id = contatos.get(contatos.size() - 1).getId();
        return id + 1;
    }

    private static boolean telefoneJaCadastrado(List<Contato> contatos, String ddd, Long numero) {
        for (Contato contato : contatos) {
            List<Telefone> telefones = contato.getTelefones();
            for (Telefone telefone : telefones) {
                if (telefone.getDdd().equals(ddd) && telefone.getNumero().equals(numero)) {
                    return true;
                }
            }
        }
        return false;
    }


    private static void rebalancearId() {
        List<Contato> contatos = carregarContatos();
        try (PrintWriter writer = new PrintWriter(new FileWriter("contatos.txt"))) {
            for (int i = 0; i < contatos.size(); i++) {
                Contato contato = contatos.get(i);
                contato.setId((long) (i + 1));
                writer.print(contato.getId() + "," + contato.getNome() + "," + contato.getSobreNome() + ",");

                List<Telefone> telefones = contato.getTelefones();
                for (int j = 0; j < telefones.size(); j++) {
                    Telefone telefone = telefones.get(j);
                    writer.print(telefone.getDdd() + "-" + telefone.getNumero());


                    if (j < telefones.size() - 1) {
                        writer.print(",");
                    }
                }

                writer.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}