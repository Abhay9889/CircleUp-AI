import { useEffect, useRef, useState } from 'react'
import * as d3 from 'd3'
import { notesApi, aiApi } from '../api'
import { GitBranch, Loader2 } from 'lucide-react'
import toast from 'react-hot-toast'

export default function MindMapPage() {
  const svgRef = useRef()
  const [notes, setNotes]     = useState([])
  const [noteId, setNoteId]   = useState('')
  const [loading, setLoading] = useState(false)
  const [graphData, setGraphData] = useState(null)

  useEffect(() => {
    notesApi.list().then(r => {
      const ready = r.data.filter(n => n.processingStatus === 'READY')
      setNotes(ready)
      if (ready.length) setNoteId(ready[0].id)
    })
  }, [])

  useEffect(() => {
    if (!graphData) return
    renderGraph(graphData)
  }, [graphData])

  const handleGenerate = async () => {
    if (!noteId) { toast.error('Select a note'); return }
    setLoading(true)
    try {
      const { data } = await aiApi.mindmap(noteId)
      setGraphData({ nodes: data.nodes, links: data.edges })
    } catch { toast.error('Mind map generation failed') }
    finally { setLoading(false) }
  }

  const renderGraph = ({ nodes, links }) => {
    const el  = svgRef.current
    const W   = el.clientWidth  || 800
    const H   = el.clientHeight || 500

    d3.select(el).selectAll('*').remove()

    const svg = d3.select(el)
      .attr('width', W).attr('height', H)

    const color = d3.scaleOrdinal(d3.schemeTableau10)

    const simulation = d3.forceSimulation(nodes)
      .force('link',   d3.forceLink(links).id(d => d.id).distance(100))
      .force('charge', d3.forceManyBody().strength(-300))
      .force('center', d3.forceCenter(W / 2, H / 2))

    const link = svg.append('g')
      .selectAll('line')
      .data(links)
      .join('line')
      .attr('stroke', '#313244')
      .attr('stroke-width', 1.5)

    const node = svg.append('g')
      .selectAll('g')
      .data(nodes)
      .join('g')
      .call(d3.drag()
        .on('start', (event, d) => { if (!event.active) simulation.alphaTarget(0.3).restart(); d.fx = d.x; d.fy = d.y })
        .on('drag',  (event, d) => { d.fx = event.x; d.fy = event.y })
        .on('end',   (event, d) => { if (!event.active) simulation.alphaTarget(0); d.fx = null; d.fy = null })
      )

    node.append('circle')
      .attr('r', d => d.type === 'root' ? 20 : 12)
      .attr('fill', d => d.type === 'root' ? '#6366f1' : color(d.type))
      .attr('fill-opacity', 0.8)

    node.append('text')
      .attr('dy', '0.35em')
      .attr('text-anchor', 'middle')
      .attr('fill', '#cdd6f4')
      .attr('font-size', d => d.type === 'root' ? '11px' : '9px')
      .text(d => d.label.length > 20 ? d.label.slice(0,20)+'…' : d.label)

    simulation.on('tick', () => {
      link
        .attr('x1', d => d.source.x).attr('y1', d => d.source.y)
        .attr('x2', d => d.target.x).attr('y2', d => d.target.y)
      node.attr('transform', d => `translate(${d.x},${d.y})`)
    })
  }

  return (
    <div className="p-6 space-y-6 animate-fade-in flex flex-col h-full">
      <div className="flex items-center justify-between flex-wrap gap-3">
        <h1 className="text-2xl font-bold text-[#cdd6f4] flex items-center gap-2">
          <GitBranch size={24} className="text-indigo-400" /> Mind Map
        </h1>
        <div className="flex items-center gap-3">
          <select value={noteId} onChange={e => setNoteId(e.target.value)} className="input py-2 w-52">
            <option value="">Select note...</option>
            {notes.map(n => <option key={n.id} value={n.id}>{n.title}</option>)}
          </select>
          <button onClick={handleGenerate} disabled={loading} className="btn-primary flex items-center gap-2">
            {loading && <Loader2 size={14} className="animate-spin" />}
            Generate
          </button>
        </div>
      </div>

      {/* SVG canvas */}
      <div className="flex-1 card p-0 overflow-hidden min-h-[400px]">
        {!graphData && !loading && (
          <div className="flex flex-col items-center justify-center h-full text-[#6c7086]">
            <GitBranch size={48} className="mb-3 opacity-30" />
            <p>Select a note and click Generate</p>
          </div>
        )}
        {loading && (
          <div className="flex flex-col items-center justify-center h-full text-[#6c7086]">
            <Loader2 size={32} className="animate-spin mb-3 text-indigo-400" />
            <p>Building mind map...</p>
          </div>
        )}
        <svg ref={svgRef} className="w-full h-full" style={{ display: graphData ? 'block' : 'none' }} />
      </div>
    </div>
  )
}
