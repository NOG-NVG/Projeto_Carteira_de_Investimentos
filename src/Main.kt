import java.io.File
import     java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

//TODO perguntar ao stor se os investimentos estiverem vazios o ficheiro guarda/nao guarda etc
//  caso guarde, guarda o que
//  se investimentos com o mesmo nome e data aglomeram-se

fun obterMenu(): String {
    return "#  Comando\n" +
            "1. Consultar\n" +
            "2. Adicionar\n" +
            "3. Editar\n" +
            "4. Liquidar\n" +
            "5. Guardar\n" +
            "9. Configuracoes\n" +
            "0. Sair\n"
}

//DONE
fun lerConfiguracoes(fileName: String, configuracoes: Array<String>): Boolean {
    val file = File(fileName)

    if (!file.exists() && !file.canRead()) {
        return false
    }

    val linhas = file.readLines()
    if (linhas.size != 2) {
        return false
    }

    configuracoes[0] = linhas[0].trim()
    configuracoes[1] = linhas[1].trim()
    return true
}

//DONE
fun guardarConfiguracoes(fileName: String, configuracoes: Array<String>) {
    val file = File(fileName)

    // Cria o ficheiro se não existir
    if (!file.exists()) {
        file.createNewFile()
    }

    file.writeText("${configuracoes[0]}\n${configuracoes[1]}")
}

fun lerLiquidacoes(fileName: String, liquidacoes: Array<Double>): Boolean {
    val file = File(fileName)
    if (!file.exists() && !file.canRead()) {
        return false
    }
    val linhas = file.readLines()
    if (linhas.size != 2) {
        return false
    }
    liquidacoes[0] = linhas[0].toDouble()
    liquidacoes[1] = linhas[1].toDouble()
    return true
    // liquidacoes[0]+=valorInvestido
    //    liquidacoes[1]+=valorInvestido* (percentagemRentabilidade/ 100)
}

fun guardarLiquidacoes(fileName: String, liquidacoes: Array<Double>) {
    val file = File(fileName)
    if (!file.exists()) {
        file.createNewFile()
    }
    file.writeText("${liquidacoes[0]}\n${liquidacoes[1]}")
}

//DONE
fun lerInvestimentos(fileName: String, investimentos: Array<Array<String>?>): Boolean {
    val file = File(fileName)
    if (!file.exists()) return false

    try {
        val linhas = file.readLines()
        var index = 0

        for (linha in linhas) {
            if (linha.isNotBlank()) {
                val partes = linha.split("|").map { it.trim() }
                if (partes.size >= 5) {
                    val nome = partes[0]
                    val data = partes[1]
                    val valorInvestido =
                        partes[2].replace("€", "").replace("$", "").replace(" ", "").replace(",", ".").trim()
                    val valorAtual =
                        partes[3].replace("€", "").replace("$", "").replace(" ", "").replace(",", ".").trim()
                    val quantidade = partes[4].replace(",", ".").trim()
                    investimentos[index] = arrayOf(nome, data, valorInvestido, valorAtual, quantidade)
                    index++
                }
            }
        }
        //index>0
        return true  // ← mesmo que não carregue nada!
    } catch (e: Exception) {
        return false
    }
}

//DONE
fun consultarInvestimentos(
    investimentos: Array<Array<String>?>,
    configuracoes: Array<String>,
    liquidacoes: Array<Double>
): String {
    var count = 0
    val moeda = configuracoes[1]
    val stringBuilder = StringBuilder()
    var maxNomeLenght = 4 // Pelo menos "Nome"

    if (investimentos[0] == null) {
        stringBuilder.append("Nao existem investimentos em carteira.\n\n")
        stringBuilder.append(String.format(Locale.US, "Lucro: %.2f %s", liquidacoes[1] - liquidacoes[0], moeda))
        return stringBuilder.toString()
    }

    // Calcular o tamanho máximo do nome
    for (i in investimentos.indices) {
        if ((investimentos[i]?.get(0)?.length ?: 0) > maxNomeLenght) {
            maxNomeLenght = investimentos[i]?.get(0)?.length ?: 0
        }
    }

    stringBuilder.append("Investimentos:\n")

    // Cabeçalho dinâmico com maxNomeLenght
    val headerFormat = "# %-${maxNomeLenght}s | %-19s | %-15s | %-11s | %-10s | %-11s\n"
    val header = String.format(
        headerFormat,
        "Nome",
        "Data",
        "Valor Investido",
        "Valor Atual",
        "Quantidade",
        "Rentabilidade"
    )
    stringBuilder.append(header)

    // Linhas de dados
    while (count < investimentos.size && investimentos[count] != null) {
        val inv = investimentos[count]!!
        val rentabilidade = if (inv[2].toDouble() != 0.0)
            ((inv[3].toDouble() * inv[4].toDouble() - inv[2].toDouble()) / inv[2].toDouble()) * 100
        else 0.0
        val rentabilidadeTabela = "%.0f".format(rentabilidade)
        val rentabilidadeFormatada = String.format("%-13s", "$rentabilidadeTabela %") // Corrigido!

        val linhaFormat = "%-1d %-${maxNomeLenght}s | %-19s | %-15s | %-11s | %-10s | %-13s\n"
        val linha = String.format(
            linhaFormat,
            count + 1,
            inv[0].trim(),
            inv[1],
            "${inv[2]} $moeda",
            "${inv[3]} $moeda",
            "%.2f".format(Locale.US, inv[4].toDouble()),
            rentabilidadeFormatada
        )
        stringBuilder.append(linha)
        count++
    }

    stringBuilder.append("\n\n")
    stringBuilder.append(String.format(Locale.US, "Lucro: %.2f %s", liquidacoes[1] - liquidacoes[0], moeda))
    return stringBuilder.toString()
}

//DONE
fun adicionarInvestimento(
    investimentos: Array<Array<String>?>,
    nome: String,
    valorInvestido: Double,
    valorAtual: Double
): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    var controller = false
    val dataHora = LocalDateTime.now().format(formatter)
    nome.trim().toUpperCase()
    //2025-03-20 11:01:15
    val novoInvestimento = arrayOf(
        nome, dataHora, valorInvestido.toString(), valorAtual.toString(), (valorInvestido / valorAtual).toString()
    )

    for (i in investimentos.indices) {
        // Se já foi preenchido, não faz mais nada
        if (controller) {

        } else {
            // Só entra aqui enquanto ainda não encontrou posição livre
            if (investimentos[i] == null) {
                investimentos[i] = novoInvestimento
                controller = true
            }
        }

    }
    if (controller) {
        return "Investimento adicionado com sucesso!"
    }
    return "\nJa tem a carteira de investimentos completa."
}

//DONE
fun editarInvestimento(investimentos: Array<Array<String>?>, nome: String, valor: Double): String {
    var controller = false
    var rentabilidade = 0.0
    for (i in investimentos.indices) {
        if ((investimentos[i]?.get(0) ?: "") == nome) {
            var quantidade = investimentos[i]?.get(4)?.toDouble()
            var investido = investimentos[i]?.get(2)?.toDouble()
            investimentos[i]?.set(3, valor.toString())
            controller = true
        }
    }

    return if (controller) {
        "Investimentos atualizados com sucesso!"
    } else {
        return "Nao existem investimentos em carteira com esse nome."
    }
}


fun liquidarInvestimento(liquidacoes: Array<Double>, investimentos: Array<Array<String>?>, numero: Int): String {
    if (numero < 1 || investimentos[numero - 1] == null || numero > investimentos.size) {
        return "Nao existem investimentos em carteira com esse numero."
    }
    val posicao = numero - 1
    val ativo = investimentos[posicao]!!
    val valorInvestido = ativo[2].replace(",", ".").toDouble()
    val valorAtual = ativo[3].replace(",", ".").toDouble()
    val quantidade = ativo[4].replace(",", ".").toDouble()
    val lucro = (valorAtual * quantidade)
    liquidacoes[0] += valorInvestido
    //liquidacoes[1]=
    liquidacoes[1] += lucro

    //println("liquidacoes[0]=${liquidacoes[0]}")
    //println("liquidacoes[1]=${liquidacoes[1]}")
    for (i in posicao until investimentos.size - 1) {
        investimentos[i] = investimentos[i + 1]
    }

    investimentos[investimentos.size - 1] = null
    // se nao \n aqui println() na main
    return "Investimento liquidado com sucesso!"
}

/*
Caso seja valido o numero da posicao:
    -remover a posicao do investimento do array (por exemplo investimentos aaa, bbb, ccc
     passam a investimentos bbb,ccc se o numero for 1)
    -passar os investimentos seguintes para a posicao anterior (aaa,bbb,ccc (1,2,3) para bbb,ccc (1,2))
    -
 */
fun guardarInvestimentos(fileName: String, investimentos: Array<Array<String>?>) {
    val file = File(fileName)

    if (!file.exists()) {
        file.createNewFile()
    }

    val stringBuilder = StringBuilder()
    val investimentosValidos = investimentos.filter { it != null && it.size >= 5 }

    for ((index, inv) in investimentosValidos.withIndex()) {
        val linha = "${inv!![0]} | ${inv[1]} | ${inv[2].replace(",", ".").toDouble()} | ${
            inv[3].replace(",", ".").toDouble()
        } | ${inv[4]}"
        stringBuilder.append(linha)
        if (index != investimentosValidos.size - 1) {
            stringBuilder.append("\n")
        }
    }

    file.writeText(stringBuilder.toString())
}


fun verificarNomeMoeda(): Array<String> {
    print("Por favor indique o seu nome:\n")
    val nomeInput = readln().trim()

    print("Por favor indique a moeda da sua conta (€ ou $):\n")
    val moedaInput = readln().trim()

    if (!nomeInput.contains(" ") || nomeInput.length < 4 || !(moedaInput == "€" || moedaInput == "$")) {
        println("Dados invalidos.")
        print(
            "O nome completo deve ser definido por pelo menos dois nomes e ter pelo menos um espaço vazio e " + "pelo menos 4 caracteres.\n"
        )
        print("A moeda deverá ser € ou $.\n\n")
        return verificarNomeMoeda()
    }

    // Só aqui se os dados forem válidos:


    return arrayOf(nomeInput, moedaInput)

}


fun verificarInvestimento(investimentos: Array<Array<String>?>) {
    var nome = ""
    var valorInvestido = 0.0
    var valorAtual = 0.0
    val valorAtualString = "Valor atual (PU):"


    print("Nome do investimento:\n")
    val nomeInput = readln().trim()
    if (!nomeValido(nomeInput)) {
        println("\nNome invalido, o nome apenas pode conter letras e tem de ter no minimo 3 caracteres.")
        println(prima())
        readln()
        print("\n")
        return verificarInvestimento(investimentos)
    }

    print("Valor investido:\n")
    val valorInvestidoInput = readln().trim()
    val parsedInvestido = valorInvestidoInput.toDoubleOrNull()
    if (parsedInvestido == null || parsedInvestido <= 0) {
        println("\nValor investido inválido! Tente novamente.\n")
        println(prima())
        return verificarInvestimento(investimentos)
    }

    println(valorAtualString)
    val valorAtualInput = readln().trim()
    println()
    val parsedAtual = valorAtualInput.toDoubleOrNull()
    if (parsedAtual == null || parsedAtual <= 0) {
        println("\nValor atual inválido! Tente novamente.\n")
        println(prima())
        return verificarInvestimento(investimentos)
    }

    // Tudo OK — prossegue
    nome = nomeInput.toUpperCase().trim()
    valorInvestido = parsedInvestido
    valorAtual = parsedAtual

    print(adicionarInvestimento(investimentos, nome, valorInvestido, valorAtual))
    print("\n${prima()}\n")

}


fun nomeValido(nome: String): Boolean {
    val nomeLimpo = nome.trim()
    return nomeLimpo.length >= 3 && nomeLimpo.matches("^[A-Za-z]+\$".toRegex())
}

fun prima():String{
    return "(prima enter para continuar)"
}

fun main() {
    var opcao = 0
    var configuracoes = arrayOf("", "")
    var nomeMoeda = arrayOf("", "")
    var liquidacoes = arrayOf(0.0, 0.0)
    var investimentos = arrayOfNulls<Array<String>>(100)
    val valorAtualString = "Valor atual (PU):"
    println("\n#####################")
    println("### Investimentos ###")
    println("#####################\n")
    lerConfiguracoes("configuracoes.txt", configuracoes)
    lerInvestimentos("investimentos.txt", investimentos)
    lerLiquidacoes("liquidacoes.txt", liquidacoes)
    if (configuracoes[0] != "") {
        println("Ola ${configuracoes[0]}\n")
    } else {
        nomeMoeda = verificarNomeMoeda()
        configuracoes[0] = nomeMoeda[0]
        configuracoes[1] = nomeMoeda[1]
        guardarConfiguracoes("configuracoes.txt", configuracoes)
        println()
        println("Ola ${configuracoes[0]}\n")
    }
    do {
        println(obterMenu())
        print("Indique o comando que pretende:\n\n")
        opcao = readln().toInt()
        when (opcao) {
            1 -> {
                print(consultarInvestimentos(investimentos, configuracoes, liquidacoes) + "\n")
                println(prima())
                readln()
            }

            2 -> {
                print("Adicionar investimento\n\n")
                verificarInvestimento(investimentos)
                readln()
            }

            3 -> {
                print("Editar investimento\n\n")
                print("Nome do investimento:\n")
                val nome = readln().trim().toUpperCase()
                println(valorAtualString)
                var valorAtual = readln()
                if (valorAtual.toDoubleOrNull() != null) {
                    println()
                    println(editarInvestimento(investimentos, nome, valorAtual.toDouble()))
                } else {
                    do {
                        print("Valor atual invalido, o valor apenas pode conter algarismos\n")
                        println(prima())
                        readln()
                        println(valorAtualString)
                        valorAtual = readln()
                    } while (valorAtual.toDoubleOrNull() == null)
                    println()
                    println(editarInvestimento(investimentos, nome, valorAtual.toDouble()))
                }
                //Caso valor errado --> "Valor atual invalido, o valor apenas pode conter algarismos" (if (!valorAtual.isNaN)...)
                println(prima())
                readln()
                obterMenu()
            }

            4 -> {
                println("Liquidar investimento")
                println("Numero do investimento:")
                val posInvestimento = readln().toInt()
                println()
                println(liquidarInvestimento(liquidacoes, investimentos, posInvestimento))
                println(prima())
                readln()
                //liquidarInvestimento() -->return string
            }

            5 -> {
                guardarInvestimentos("investimentos.txt", investimentos)
                guardarConfiguracoes("configuracoes.txt", configuracoes)
                guardarLiquidacoes("liquidacoes.txt", liquidacoes)
                print("Investimentos guardados com sucesso.\n")
                println(prima())
                readln()
            }

            9 -> {
                print("Configuracoes\n\n")
                nomeMoeda = verificarNomeMoeda()
                configuracoes[0] = nomeMoeda[0]
                configuracoes[1] = nomeMoeda[1]
                guardarConfiguracoes("configuracoes.txt", configuracoes)
                println()
                print("Configuracoes atualizadas com sucesso!\n")
                println(prima())
                readln()

            }

        }
        if (opcao !in 0..5 && opcao != 9) {
            print("Opcao invalida!\n${prima()}\n")
            readln()
            obterMenu()
        }
    } while (opcao != 0)

    //println("liquidacoes[0]=${liquidacoes[0]}")
    //println("liquidacoes[1]=${liquidacoes[1]}")

    //guardarLiquidacoes("liquidacoes.txt", liquidacoes)
}