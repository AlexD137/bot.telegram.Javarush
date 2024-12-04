package com.javarush.telegram;

import com.javarush.telegram.ChatGPTService;
import com.javarush.telegram.DialogMode;
import com.javarush.telegram.MultiSessionTelegramBot;
import com.javarush.telegram.UserInfo;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;

public class TinderBoltApp extends MultiSessionTelegramBot {
    public static final String TELEGRAM_BOT_NAME = "super1_java_tinder_bot";
    public static final String TELEGRAM_BOT_TOKEN = "7443751670:AAEDqPTKD8wCgw-xmonjEYFkkumubEqybAk";
    public static final String OPEN_AI_TOKEN = "sk-proj-2ddMbnkrUp0r1ZBktBwHT3BlbkFJN9vmd9QH66u8DBihTVMH";
    private  ChatGPTService chatGPT = new ChatGPTService(OPEN_AI_TOKEN);
    private  DialogMode currentMod = null;
    private ArrayList<String> list = new ArrayList<>();
    private  UserInfo me;
    private  UserInfo she;
    private int questionCount;
    public TinderBoltApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }

    @Override
    public void onUpdateEventReceived(Update update) {

        String message = getMessageText();
        if (message.equals("/start")){
            currentMod = DialogMode.MAIN;
            sendPhotoMessage("main");
            String text = loadMessage("main");
            sendTextMessage(text);
            showMainMenu( "главное меню бота", "/start",
                    "генерация Tinder-профля \uD83D\uDE0E", " /profile",
                    "сообщение для знакомства \uD83E\uDD70", "/opener",
                    "переписка от вашего имени \uD83D\uDE08", "/message",
                    "переписка со звездами \uD83D\uDD25", "/date",
                    "задать вопрос чату GPT \uD83E\uDDE0", "/gpt");
            return;
        }
        if (message.equals("/gpt")){
            currentMod = DialogMode.GPT;
            sendPhotoMessage("gpt");
            String text = loadMessage("gpt");
            sendTextMessage(text);

        }
        if ( currentMod == DialogMode.GPT && !isMessageCommand()){
            String prompt = loadPrompt("gpt");
            Message msg = sendTextMessage("Подождите пау секунд chatGPT Думает...");
            String answer = chatGPT.sendMessage(prompt, message);
            updateTextMessage(msg, answer);
            return;}
            // command DATE

            if ( message.equals("/date")){
                currentMod = DialogMode.DATE;
                sendPhotoMessage("date");
                String text = loadMessage("date");
                sendTextButtonsMessage(text,
                        "Ариана Гранде", "date_grande",
                        "Марого Робби", "date_robbie",
                        "Зендея", "date_zendaya",
                        "Райн Гослинг", "date_gosling",
                        "Том Харди", "date_hardy");
                return;
            }
            if( currentMod == DialogMode.DATE && !isMessageCommand()){
                String query = getCallbackQueryButtonKey();
                if ( query.startsWith("date_")){
                    sendPhotoMessage(query);
                    sendTextMessage(" Отличный выбор! \uD83D\uDE05 \n* Вы должны пригоасить девушку/парня на свидание ❤\uFE0F за 5 сообщений.*\n Первый шаг за Вами:");
                    String prompt = loadPrompt(query);
                    chatGPT.setPrompt(prompt);
                    return;
                }
                Message nsg = sendTextMessage("Подождите девушка набирает текст...");
                String answer = chatGPT.addMessage(message);
                updateTextMessage(nsg, answer);
                return;
            }
            //command Message

        if( message.equals("/message")){
            currentMod = DialogMode.MESSAGE;
            sendPhotoMessage("message");
            sendTextButtonsMessage("Пришлите в чат Вашу переписку:",
                    "Следующее сообщение","message_next",
                    "Пригласить на свидание ", "message_date");
            return;
        }
        if( currentMod == DialogMode.MESSAGE && !isMessageCommand()){
            String query = getCallbackQueryButtonKey();
            if (query.startsWith("message_")){
                String prompt = loadPrompt(query);
                String userChatHistory = String.join("\n\n", list);
                Message msg = sendTextMessage("Подождите пау секунд chatGPT Думает...");
                String answer = chatGPT.sendMessage(prompt, userChatHistory);
                updateTextMessage(msg, answer);
                return;

            }
            list.add(message);
            return;

        }
        // command PROFILE
        if (message.equals("/profile")){
            currentMod = DialogMode.PROFILE;
            sendPhotoMessage("profile");
            me = new UserInfo();
            questionCount = 1;
            sendTextMessage("Сколько Вам лет?");
            return;
        }
        if( currentMod == DialogMode.PROFILE && !isMessageCommand()){
            switch (questionCount){
                case 1:
                    me.age = message;
                    questionCount = 2;
                    sendTextMessage("Кем Вы работаете?");
                    return;
                case 2:
                    me.occupation = message;
                    questionCount = 3;
                    sendTextMessage("У Вас есть хобби?");
                    return;
                case 3:
                    me.hobby = message;
                    questionCount = 4;
                    sendTextMessage("Что Вам не нравится в людях?");
                    return;
                case 4:
                    me.annoys = message;
                    questionCount = 5;
                    sendTextMessage("Цель знакомства?");
                    return;
                case 5:
                    me.goals = message;
                    String aboutMyself = me.toString();
                    String prompt = loadPrompt("profile");
                    Message msg = sendTextMessage("Подождите пару секунд, ChatGPT \uD83E\uDDE0 думает...");
                    String answer =chatGPT.sendMessage(prompt, aboutMyself);
                    updateTextMessage(msg, answer);
                    return;
            }
            return;
        }

        if (message.equals("/opener")){
            currentMod = DialogMode.OPENER;
            sendPhotoMessage("opener");
            she = new UserInfo();
            questionCount = 1;
            sendTextMessage("Имя девушки?");
            return;
        }
        if( currentMod == DialogMode.OPENER && !isMessageCommand()){
            switch (questionCount){
                case 1:
                    she.name = message;
                    questionCount = 2;
                    sendTextMessage("Сколько ей лет?");
                    return;
                case 2:
                    she.age = message;
                    questionCount = 3;
                    sendTextMessage("Есть ли у нее хобби?");
                    return;
                case 3:
                    she.hobby = message;
                    questionCount = 4;
                    sendTextMessage("Кем она работает?");
                    return;
                case 4:
                    she.occupation = message;
                    questionCount = 5;
                    sendTextMessage("Цель знакомства?");
                    return;
                case 5:
                    she.goals = message;
                    String aboutFriend = she.toString();
                    String prompt = loadPrompt("opener");
                    Message msg = sendTextMessage("Подождите пару секунд, ChatGPT \uD83E\uDDE0 думает...");
                    String answer = chatGPT.sendMessage(prompt, aboutFriend);
                    updateTextMessage(msg, answer);
                    return;
            }
            return;
        }


        sendTextMessage("*Привет*");
        sendTextMessage("_Привет_");
        sendTextMessage("Вы написали " + message);
        sendTextButtonsMessage("*Выберите режим работы*",
                "Старт", "start",
                "Стоп", "stop" );

    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }
}
