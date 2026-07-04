from kivy.app import App
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.button import Button
from kivy.uix.label import Label
from kivy.uix.textinput import TextInput
from kivy.uix.scrollview import ScrollView
from kivy.uix.gridlayout import GridLayout
from kivy.metrics import dp


class MoneyMateApp(App):
    def build(self):
        self.salary = 5000
        self.expenses = []
        self.ai_tip = "Enter your OpenAI API key and expense details to get personalized advice."

        root = BoxLayout(orientation='vertical', padding=dp(16), spacing=dp(12))

        title = Label(text='MoneyMate', font_size='24sp', bold=True, size_hint_y=None, height=dp(40))
        root.add_widget(title)

        self.salary_input = TextInput(text='5000', hint_text='Monthly salary', multiline=False)
        root.add_widget(self.salary_input)

        self.expense_name = TextInput(hint_text='Expense name', multiline=False)
        root.add_widget(self.expense_name)

        self.expense_amount = TextInput(hint_text='Expense amount', multiline=False)
        root.add_widget(self.expense_amount)

        self.api_key_input = TextInput(hint_text='OpenAI API key', multiline=False, password=True)
        root.add_widget(self.api_key_input)

        self.tip_label = Label(text=self.ai_tip, size_hint_y=None, height=dp(100), text_size=(None, None))
        root.add_widget(self.tip_label)

        button_row = BoxLayout(size_hint_y=None, height=dp(50), spacing=dp(10))
        add_button = Button(text='Add Expense')
        add_button.bind(on_press=self.add_expense)
        button_row.add_widget(add_button)

        ai_button = Button(text='Ask AI')
        ai_button.bind(on_press=self.ask_ai)
        button_row.add_widget(ai_button)
        root.add_widget(button_row)

        self.expense_list = GridLayout(cols=1, spacing=dp(8), size_hint_y=None)
        self.expense_list.bind(minimum_height=self.expense_list.setter('height'))

        scroll = ScrollView(size_hint=(1, 1))
        scroll.add_widget(self.expense_list)
        root.add_widget(scroll)

        self.update_summary()
        return root

    def add_expense(self, instance):
        try:
            amount = float(self.expense_amount.text)
            name = self.expense_name.text.strip()
            if name and amount > 0:
                self.expenses.append((name, amount))
                self.expense_name.text = ''
                self.expense_amount.text = ''
                self.update_summary()
        except ValueError:
            self.ai_tip = 'Please enter a valid expense amount.'
            self.tip_label.text = self.ai_tip

    def update_summary(self):
        total = sum(amount for _, amount in self.expenses)
        try:
            salary = float(self.salary_input.text)
            self.salary = salary
        except ValueError:
            self.salary = 0
        remaining = self.salary - total
        self.tip_label.text = f'Total expenses: {total}\nRemaining balance: {remaining}\n{self.ai_tip}'

    def ask_ai(self, instance):
        try:
            amount = float(self.expense_amount.text or '0')
            name = self.expense_name.text.strip() or 'expense'
            api_key = self.api_key_input.text.strip()
            if not api_key:
                self.ai_tip = 'Please enter your OpenAI API key.'
                self.update_summary()
                return
            if amount <= 0:
                self.ai_tip = 'Please enter a valid expense amount.'
                self.update_summary()
                return

            import requests

            url = 'https://api.openai.com/v1/chat/completions'
            headers = {
                'Authorization': f'Bearer {api_key}',
                'Content-Type': 'application/json'
            }
            data = {
                'model': 'gpt-4o-mini',
                'messages': [
                    {'role': 'system', 'content': 'You give short, practical money-saving advice.'},
                    {'role': 'user', 'content': f'My salary is {self.salary}. I spent {amount} on {name}. Give me one concise saving tip.'}
                ]
            }
            response = requests.post(url, headers=headers, json=data, timeout=20)
            if response.ok:
                import json
                content = response.json()['choices'][0]['message']['content']
                self.ai_tip = content
            else:
                self.ai_tip = 'AI request failed. Check your API key and internet connection.'
        except Exception as e:
            self.ai_tip = f'Error: {e}'

        self.update_summary()


if __name__ == '__main__':
    MoneyMateApp().run()
