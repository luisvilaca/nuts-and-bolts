# Sync e publicação (Firebase)

A app sincroniza o tablet e o telemóvel pela tua conta Google (Firebase Auth + Firestore). As versões novas chegam aos dois aparelhos pelo Firebase App Distribution. Sem `app/google-services.json` a app compila e funciona como antes, só sem sync ("Sync is not set up in this build" nas Definições).

Fica tudo no plano gratuito (Spark): 1 GiB no Firestore, 50 000 leituras e 20 000 escritas por dia. Para uma pessoa chega e sobra.

## 1. Criar o projeto

1. Abre <https://console.firebase.google.com> com a conta Google que vais usar nos dois aparelhos.
2. **Criar projeto**, com o nome `nuts-and-bolts`. O Google Analytics não é preciso.

## 2. Registar a app Android

1. No projeto, carrega em **Adicionar app > Android**.
2. Preenche o registo:
   - **Nome do pacote:** `com.luisvilaca.nutsandbolts`
   - **SHA-1:** `AB:FF:AD:CC:CD:B4:84:C0:04:89:F3:DA:DE:53:4C:99:55:7D:85:AA`
     - É a chave que assina a app neste PC, `~/.android/debug.keystore`.
     - Sem o SHA-1 certo, "Sign in with Google" falha.
     - Junta também o SHA-256 em **Definições do projeto > A tua app > Adicionar impressão digital**: `94:65:6C:33:36:D3:99:5C:71:F8:C9:1E:E0:11:E1:50:AF:0D:BB:0F:CB:8B:47:B7:EC:73:C2:22:E6:23:28:6E`
3. Ainda **não** descarregues o `google-services.json`: faz primeiro o passo 3, para o ficheiro já trazer o cliente do login Google.

## 3. Ligar o login Google

1. Vai a **Authentication > Começar > Método de início de sessão > Google**.
2. Ativa-o, escolhe o email de suporte e guarda.
3. Agora, em **Definições do projeto > As tuas apps**, descarrega o `google-services.json`.
4. Põe-no em `C:\NutsAndBolts\app\google-services.json`. Fica fora do git (`.gitignore`).

## 4. Criar a base de dados

1. Vai a **Firestore Database > Criar base de dados**.
2. Escolhe a localização `eur3 (Europe)` ou `europe-west1` e o **modo de produção**.
3. Em **Regras**, cola isto e publica. Cada conta só lê e escreve os seus próprios dados:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{uid}/{document=**} {
      allow read, write: if request.auth != null && request.auth.uid == uid;
    }
    // o cartão de Parceiro (passo 7): cada um escreve o seu; só o lê quem o cartão nomeia
    match /peek/{uid} {
      allow write: if request.auth != null && request.auth.uid == uid;
      allow read: if request.auth != null
        && (request.auth.uid == uid || resource.data.buddy == request.auth.token.email);
    }
  }
}
```

## 5. App Distribution: instalar e atualizar nos dois aparelhos

1. Vai a **App Distribution > Começar**.
2. Em **Testadores e grupos**, junta o teu email. O `local.properties` já tem `appDistributionTesters=luismvilaca21@gmail.com`; acrescenta outros separados por vírgulas.
3. Neste PC, uma vez só, instala e liga o Firebase CLI. Na linha de comandos do Claude Code podes escrever `! npm install -g firebase-tools` e depois `! firebase login`.
4. Para publicar uma versão:
   ```
   .\tools\publish.ps1 "O que mudou"
   ```
   O script corre os testes, faz o APK release e envia-o.
5. Nos aparelhos, o primeiro email da App Distribution traz um convite:
   - Aceita-o no tablet e no telemóvel e instala a app **Firebase App Tester** que ele indica.
   - Daí em diante, cada versão nova aparece lá e instala-se com um toque.
   - No HyperOS, permite "instalar apps desconhecidas" à App Tester.

Cada build tem como `versionCode` a hora em que foi feito (yyMMddHH), por isso instala sempre por cima da anterior. Todas têm de ser assinadas com a mesma chave, a deste PC. Se mudares de PC, copia `~/.android/debug.keystore`; sem ela, as atualizações já não instalam por cima.

## 6. Ligar a sync nos aparelhos

1. **Começa pelo tablet**, que é onde estão os teus dados: abre **Definições > Sync > Sign in with Google**.
   - Na primeira vez sobe tudo o que lá está: hábitos, robô, peças e páginas do diário.
2. No telemóvel, entra com a mesma conta.
   - Como a nuvem já tem dados, o telemóvel fica com a cópia da nuvem.
   - Se o telemóvel já tinha hábitos ou páginas, ficam guardados antes em **Downloads/nuts-and-bolts-before-sync-….json**. Podes repô-los em **Definições > Restore a backup**.
3. Daí em diante, cada alteração passa para o outro aparelho em segundos.
   - Offline, as alterações ficam em fila e seguem quando houver rede.
   - Quando os dois aparelhos mudam coisas diferentes (hábitos diferentes, dias diferentes), juntam-se sem perder nada.
   - Os bolts, nuts e atributos são recalculados a partir das marcas em cada aparelho, por isso nunca contam um Done duas vezes.

Ficam só em cada aparelho as definições da caneta (palm rejection, botões, espessuras), o interruptor dos sons e a data do último backup.

## HyperOS: notificações a horas

Os lembretes por hábito usam alarmes exatos locais e funcionam sem rede. O HyperOS, no entanto, mata apps em segundo plano. Em cada aparelho, em **Definições > Apps > Nuts And Bolts**:

- ativa **Arranque automático**;
- em **Poupança de bateria**, escolhe **Sem restrições**;
- nas **Notificações**, deixa "Habit reminders" com som.

No telemóvel dela faz o mesmo em **Five Nights at the Circus**.

## 7. A app dela (Five Nights at the Circus) e o Parceiro

A mesma app, com o visual do circo, é a variante `circus` do projeto. Instala-se ao lado da tua (pacote próprio) e tem os seus próprios dados, na conta Google dela.

1. **Registar a app dela** no mesmo projeto Firebase: **Adicionar app > Android**.
   - **Nome do pacote:** `com.luisvilaca.nutsandbolts.circus`
   - **SHA-1** e **SHA-256**: os mesmos do passo 2 (é a mesma chave deste PC).
2. Em **Definições do projeto > As tuas apps**, descarrega outra vez o `google-services.json` (agora traz as duas apps) e substitui `C:\NutsAndBolts\app\google-services.json`.
   - Até lá, a variante do circo compila na mesma, só sem sync nem Parceiro.
3. Em **Firestore > Regras**, cola as regras do passo 4 (agora com o bloco `peek`) e publica.
4. **App Distribution:** no `local.properties`, junta `appDistributionTestersCircus=` com o email dela. Para publicar a versão dela:
   ```
   .\tools\publish.ps1 "O que mudou" circus
   ```
   - Ela recebe o convite, instala a **Firebase App Tester** e daí em diante atualiza com um toque.
   - No Android Studio, escolhe a variante em **Build Variants**: `workshopDebug` é a tua, `circusDebug` é a dela.
5. **No telemóvel dela:** **Definições > Sync > Sign in with Google** com a conta dela.
   - Se ela preferir não usar a conta Google: **Or use an email and password**, com qualquer email (Yandex, Mail.ru…), e **Create account** na primeira vez. Para isto funcionar, liga uma vez no Firebase: **Authentication > Sign-in method > Adicionar fornecedor > Email/palavra-passe > Ativar > Guardar**.
   - Na Rússia, a sync, o Parceiro e o login (qualquer um dos dois) precisam da VPN ligada, porque passam pelos servidores da Google. Sem VPN a app funciona na mesma e envia tudo quando a VPN voltar.
6. **Ligar os dois (Parceiro):**
   - Na app dela, em **Definições > Parceiro**, ela escreve o teu email Google e carrega em **Ligar**.
   - Na tua, escreves o email dela.
   - Quando os dois se tiverem nomeado, cada um vê no ecrã inicial o personagem do outro e quantos hábitos fez hoje, com um botão **Incentivar**.
   - O incentivo chega como notificação enquanto a app do outro está aberta, ou na próxima vez que a abrir. Para chegar com a app fechada seria preciso o plano Blaze (Cloud Functions).
