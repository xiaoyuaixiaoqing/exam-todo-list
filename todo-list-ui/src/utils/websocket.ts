export class TaskWebSocket {
  private ws: WebSocket | null = null
  private listeners: ((data: any) => void)[] = []

  connect() {
    this.ws = new WebSocket('ws://localhost/ws/task')

    this.ws.onmessage = (event) => {
      const data = JSON.parse(event.data)
      this.listeners.forEach(listener => listener(data))
    }

    this.ws.onerror = () => {
      setTimeout(() => this.connect(), 3000)
    }

    this.ws.onclose = () => {
      setTimeout(() => this.connect(), 3000)
    }
  }

  onMessage(listener: (data: any) => void) {
    this.listeners.push(listener)
  }

  disconnect() {
    this.ws?.close()
  }
}
