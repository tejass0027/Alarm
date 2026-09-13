from balance import LocalVirtualProvider
from storage import Storage
from ui_main import MainWindow


def main():
    storage = Storage()
    provider = LocalVirtualProvider(storage)
    app = MainWindow(storage, provider)
    app.mainloop()


if __name__ == "__main__":
    main()
